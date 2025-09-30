package com.vendorauth.config;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableConfigurationProperties(AuthSigningProperties.class)
public class KeystoreJwksConfig {

    @Bean
    public JWKSource<SecurityContext> jwkSource(AuthSigningProperties p, ResourceLoader loader) throws Exception {
        try (InputStream in = loader.getResource(p.getKeystore()).getInputStream()) {
            KeyStore ks = KeyStore.getInstance(p.getType()); // JKS or PKCS12
            ks.load(in, p.getStorePassword().toCharArray());

            List<JWK> keys = new ArrayList<>();
            keys.add(buildJwkFromAlias(ks, p.getActiveAlias(), p.getKeyPassword(), p.getKid()));

            if (p.getPreviousAlias() != null && !p.getPreviousAlias().isBlank()) {
                keys.add(buildJwkFromAlias(ks, p.getPreviousAlias(), p.getKeyPassword(), null));
            }

            return new ImmutableJWKSet<>(new JWKSet(keys));
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSetUri(null).build();
    }

    private static JWK buildJwkFromAlias(KeyStore ks, String alias, String keyPassword, String kidOverride) throws Exception {
        PrivateKey priv = (PrivateKey) ks.getKey(alias, keyPassword.toCharArray());
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        PublicKey pub = cert.getPublicKey();

        String kid = (kidOverride != null && !kidOverride.isBlank()) ? kidOverride : deriveKidFromCert(cert);
        String kidFinal = kid + "-" + alias; // keep kids distinct when publishing multiple keys

        if (priv instanceof java.security.interfaces.RSAPrivateKey && pub instanceof java.security.interfaces.RSAPublicKey) {
            return new RSAKey.Builder((java.security.interfaces.RSAPublicKey) pub)
                    .privateKey(priv)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(kidFinal)
                    .build();
        }

        if (priv instanceof java.security.interfaces.ECPrivateKey && pub instanceof java.security.interfaces.ECPublicKey) {
            Curve curve = curveOf((java.security.interfaces.ECPublicKey) pub); // P-256 / P-384 / P-521
            return new ECKey.Builder(curve, (java.security.interfaces.ECPublicKey) pub)
                    .privateKey(priv)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(algForCurve(curve)) // ES256/384/512
                    .keyID(kidFinal)
                    .build();
        }

        throw new IllegalStateException("Unsupported key type for alias '" + alias + "'.");
    }

    private static String deriveKidFromCert(X509Certificate cert) throws Exception {
        byte[] sha = java.security.MessageDigest.getInstance("SHA-256").digest(cert.getEncoded());
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(sha).substring(0, 10);
    }

    private static Curve curveOf(java.security.interfaces.ECPublicKey pub) {
        int bits = pub.getParams().getCurve().getField().getFieldSize();
        return switch (bits) {
            case 256 -> Curve.P_256;
            case 384 -> Curve.P_384;
            case 521 -> Curve.P_521;
            default -> throw new IllegalStateException("Unsupported EC curve size: " + bits);
        };
    }

    private static JWSAlgorithm algForCurve(Curve c) {
        if (Curve.P_256.equals(c)) return JWSAlgorithm.ES256;
        if (Curve.P_384.equals(c)) return JWSAlgorithm.ES384;
        if (Curve.P_521.equals(c)) return JWSAlgorithm.ES512;
        throw new IllegalStateException("Unsupported EC curve: " + c);
    }
}
