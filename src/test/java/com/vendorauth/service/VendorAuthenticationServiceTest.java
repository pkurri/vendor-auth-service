package com.vendorauth.service;

import com.vendorauth.entity.VendorConfig;
import com.vendorauth.enums.AuthType;
import com.vendorauth.repository.VendorConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorAuthenticationServiceTest {

    @Mock
    private VendorConfigRepository vendorConfigRepository;

    @InjectMocks
    private VendorAuthenticationService vendorAuthenticationService;

    private VendorConfig vendor1;
    private VendorConfig vendor2;

    @BeforeEach
    void setUp() {
        vendor1 = new VendorConfig();
        vendor1.setId(1L);
        vendor1.setVendorId("test-vendor-1");
        vendor1.setVendorName("Test Vendor 1");
        vendor1.setAuthType(AuthType.OAUTH2);
        vendor1.setActive(true);

        vendor2 = new VendorConfig();
        vendor2.setId(2L);
        vendor2.setVendorId("test-vendor-2");
        vendor2.setVendorName("Test Vendor 2");
        vendor2.setAuthType(AuthType.API_KEY);
        vendor2.setActive(true);
    }

    @Test
    void getAllVendors_ShouldReturnAllVendors() {
        // given
        when(vendorConfigRepository.findAll()).thenReturn(Arrays.asList(vendor1, vendor2));

        // when
        List<VendorConfig> vendors = vendorAuthenticationService.getAllVendors();

        // then
        assertThat(vendors).hasSize(2);
        verify(vendorConfigRepository, times(1)).findAll();
    }

    @Test
    void getVendorById_WithExistingId_ShouldReturnVendor() {
        // given
        when(vendorConfigRepository.findById(1L)).thenReturn(Optional.of(vendor1));

        // when
        Optional<VendorConfig> found = vendorAuthenticationService.getVendorById(1L);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getVendorId()).isEqualTo("test-vendor-1");
    }

    @Test
    void getVendorById_WithNonExistingId_ShouldReturnEmpty() {
        // given
        when(vendorConfigRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        Optional<VendorConfig> found = vendorAuthenticationService.getVendorById(999L);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void getVendorByVendorId_WithExistingVendorId_ShouldReturnVendor() {
        // given
        when(vendorConfigRepository.findByVendorId("test-vendor-1")).thenReturn(Optional.of(vendor1));

        // when
        Optional<VendorConfig> found = vendorAuthenticationService.getVendorByVendorId("test-vendor-1");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(1L);
    }

    @Test
    void createVendor_ShouldSaveAndReturnVendor() {
        // given
        when(vendorConfigRepository.save(any(VendorConfig.class))).thenReturn(vendor1);

        // when
        VendorConfig created = vendorAuthenticationService.createVendor(vendor1);

        // then
        assertThat(created).isNotNull();
        assertThat(created.getVendorId()).isEqualTo("test-vendor-1");
        verify(vendorConfigRepository, times(1)).save(vendor1);
    }

    @Test
    void updateVendor_WithExistingId_ShouldUpdateAndReturnVendor() {
        // given
        when(vendorConfigRepository.existsById(1L)).thenReturn(true);
        when(vendorConfigRepository.save(any(VendorConfig.class))).thenReturn(vendor1);

        // when
        Optional<VendorConfig> updated = vendorAuthenticationService.updateVendor(1L, vendor1);

        // then
        assertThat(updated).isPresent();
        verify(vendorConfigRepository, times(1)).save(vendor1);
    }

    @Test
    void updateVendor_WithNonExistingId_ShouldReturnEmpty() {
        // given
        when(vendorConfigRepository.existsById(999L)).thenReturn(false);

        // when
        Optional<VendorConfig> updated = vendorAuthenticationService.updateVendor(999L, vendor1);

        // then
        assertThat(updated).isEmpty();
        verify(vendorConfigRepository, never()).save(any());
    }

    @Test
    void deleteVendor_WithExistingId_ShouldDeleteVendor() {
        // given
        when(vendorConfigRepository.existsById(1L)).thenReturn(true);
        doNothing().when(vendorConfigRepository).deleteById(1L);

        // when
        boolean deleted = vendorAuthenticationService.deleteVendor(1L);

        // then
        assertTrue(deleted);
        verify(vendorConfigRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteVendor_WithNonExistingId_ShouldReturnFalse() {
        // given
        when(vendorConfigRepository.existsById(999L)).thenReturn(false);

        // when
        boolean deleted = vendorAuthenticationService.deleteVendor(999L);

        // then
        assertFalse(deleted);
        verify(vendorConfigRepository, never()).deleteById(any());
    }

    @Test
    void getVendorsByAuthType_ShouldReturnFilteredVendors() {
        // given
        when(vendorConfigRepository.findByAuthType(AuthType.OAUTH2)).thenReturn(List.of(vendor1));

        // when
        List<VendorConfig> vendors = vendorAuthenticationService.getVendorsByAuthType(AuthType.OAUTH2);

        // then
        assertThat(vendors).hasSize(1);
        assertThat(vendors.get(0).getAuthType()).isEqualTo(AuthType.OAUTH2);
        verify(vendorConfigRepository, times(1)).findByAuthType(AuthType.OAUTH2);
    }
}
