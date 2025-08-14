package com.vendorauth.repository;

import com.vendorauth.entity.VendorConfig;
import com.vendorauth.enums.AuthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class VendorConfigRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VendorConfigRepository vendorConfigRepository;

    private VendorConfig activeOauthVendor;
    private VendorConfig inactiveOauthVendor;
    private VendorConfig activeApiVendor;

    @BeforeEach
    void setUp() {
        // Common test data setup
        activeOauthVendor = createVendor("test-oauth-1", "OAuth Vendor 1", AuthType.OAUTH2, true);
        inactiveOauthVendor = createVendor("test-oauth-2", "OAuth Vendor 2", AuthType.OAUTH2, false);
        activeApiVendor = createVendor("test-api-1", "API Vendor 1", AuthType.API_KEY, true);
        
        entityManager.persist(activeOauthVendor);
        entityManager.persist(inactiveOauthVendor);
        entityManager.persist(activeApiVendor);
        entityManager.flush();
    }

    private VendorConfig createVendor(String vendorId, String vendorName, AuthType authType, boolean active) {
        return VendorConfig.builder()
                .vendorId(vendorId)
                .vendorName(vendorName)
                .authType(authType)
                .active(active)
                .build();
    }

    @Test
    void whenFindByVendorId_thenReturnVendorConfig() {
        // when
        Optional<VendorConfig> found = vendorConfigRepository.findByVendorId(activeOauthVendor.getVendorId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getVendorId()).isEqualTo(activeOauthVendor.getVendorId());
    }

    @Test
    void whenFindByVendorId_thenReturnEmpty() {
        // when
        Optional<VendorConfig> found = vendorConfigRepository.findByVendorId("non-existent-id");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void whenFindByAuthType_thenReturnVendors() {
        // when
        List<VendorConfig> found = vendorConfigRepository.findByAuthType(AuthType.OAUTH2);

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(VendorConfig::getAuthType).containsOnly(AuthType.OAUTH2);
    }

    @Test
    void whenFindByAuthTypeAndActiveTrue_thenReturnActiveVendors() {
        // when
        List<VendorConfig> found = vendorConfigRepository.findByAuthTypeAndActiveTrue(AuthType.OAUTH2);

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getVendorId()).isEqualTo("test-oauth-1");
        assertThat(found.get(0).isActive()).isTrue();
    }

    @Test
    void whenFindByActiveTrue_thenReturnActiveVendors() {
        // when
        List<VendorConfig> found = vendorConfigRepository.findByActiveTrue();

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(VendorConfig::isActive).containsOnly(true);
        assertThat(found).extracting(VendorConfig::getVendorId)
                .containsExactlyInAnyOrder("test-oauth-1", "test-api-1");
    }

    @Test
    void whenExistsByVendorId_thenReturnTrue() {
        // when
        boolean exists = vendorConfigRepository.existsByVendorId(activeOauthVendor.getVendorId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void whenExistsByVendorId_thenReturnFalse() {
        // when
        boolean exists = vendorConfigRepository.existsByVendorId("non-existent-id");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void whenFindByVendorNameContainingIgnoreCase_thenReturnMatchingVendors() {
        // when
        List<VendorConfig> found = vendorConfigRepository.findByVendorNameContainingIgnoreCase("oauth");

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(VendorConfig::getVendorName)
                .containsExactlyInAnyOrder("OAuth Vendor 1", "OAuth Vendor 2");
    }

    @Test
    void whenSaveVendor_thenVendorIsSaved() {
        // given
        VendorConfig newVendor = createVendor("new-vendor", "New Vendor", AuthType.BASIC, true);

        // when
        VendorConfig saved = vendorConfigRepository.save(newVendor);
        Optional<VendorConfig> found = vendorConfigRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getVendorId()).isEqualTo("new-vendor");
        assertThat(found.get().getVendorName()).isEqualTo("New Vendor");
    }
}
