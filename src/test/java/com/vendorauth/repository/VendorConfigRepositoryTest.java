package com.vendorauth.repository;

import com.vendorauth.entity.VendorConfig;
import com.vendorauth.enums.AuthType;
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

    @Test
    void whenFindByVendorId_thenReturnVendorConfig() {
        // given
        VendorConfig vendor = new VendorConfig();
        vendor.setVendorId("test-vendor-1");
        vendor.setVendorName("Test Vendor");
        vendor.setAuthType(AuthType.OAUTH2);
        entityManager.persistAndFlush(vendor);

        // when
        Optional<VendorConfig> found = vendorConfigRepository.findByVendorId(vendor.getVendorId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getVendorId()).isEqualTo(vendor.getVendorId());
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
        // given
        VendorConfig vendor1 = new VendorConfig();
        vendor1.setVendorId("test-vendor-1");
        vendor1.setVendorName("Test Vendor 1");
        vendor1.setAuthType(AuthType.OAUTH2);
        entityManager.persistAndFlush(vendor1);

        VendorConfig vendor2 = new VendorConfig();
        vendor2.setVendorId("test-vendor-2");
        vendor2.setVendorName("Test Vendor 2");
        vendor2.setAuthType(AuthType.OAUTH2);
        entityManager.persistAndFlush(vendor2);

        // when
        List<VendorConfig> found = vendorConfigRepository.findByAuthType(AuthType.OAUTH2);

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(VendorConfig::getAuthType).containsOnly(AuthType.OAUTH2);
    }

    @Test
    void whenFindByActiveTrue_thenReturnActiveVendors() {
        // given
        VendorConfig activeVendor = new VendorConfig();
        activeVendor.setVendorId("active-vendor");
        activeVendor.setVendorName("Active Vendor");
        activeVendor.setAuthType(AuthType.API_KEY);
        activeVendor.setActive(true);
        entityManager.persistAndFlush(activeVendor);

        VendorConfig inactiveVendor = new VendorConfig();
        inactiveVendor.setVendorId("inactive-vendor");
        inactiveVendor.setVendorName("Inactive Vendor");
        inactiveVendor.setAuthType(AuthType.API_KEY);
        inactiveVendor.setActive(false);
        entityManager.persistAndFlush(inactiveVendor);

        // when
        List<VendorConfig> found = vendorConfigRepository.findByActiveTrue();

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).isActive()).isTrue();
        assertThat(found.get(0).getVendorId()).isEqualTo("active-vendor");
    }

    @Test
    void whenSaveVendor_thenVendorIsSaved() {
        // given
        VendorConfig vendor = new VendorConfig();
        vendor.setVendorId("new-vendor");
        vendor.setVendorName("New Vendor");
        vendor.setAuthType(AuthType.BASIC);
        vendor.setActive(true);

        // when
        VendorConfig saved = vendorConfigRepository.save(vendor);
        Optional<VendorConfig> found = vendorConfigRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getVendorId()).isEqualTo("new-vendor");
        assertThat(found.get().getVendorName()).isEqualTo("New Vendor");
    }
}
