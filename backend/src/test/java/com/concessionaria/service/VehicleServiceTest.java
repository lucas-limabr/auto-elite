package com.concessionaria.service;

import com.concessionaria.dto.VehicleEligibilityResponse;
import com.concessionaria.dto.VehicleRequest;
import com.concessionaria.dto.VehicleResponse;
import com.concessionaria.entity.*;
import com.concessionaria.enums.FuelType;
import com.concessionaria.enums.TransmissionType;
import com.concessionaria.enums.VehicleStatus;
import com.concessionaria.exception.BusinessException;
import com.concessionaria.exception.ResourceNotFoundException;
import com.concessionaria.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DealershipBranchRepository branchRepository;

    @Mock
    private VehicleFeatureRepository featureRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private VehicleService vehicleService;

    private Brand brandBmw;
    private Category suvCategory;
    private Category electricCategory;
    private DealershipBranch branch;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        brandBmw = Brand.builder().id(1L).name("BMW").active(true).build();
        suvCategory = Category.builder().id(1L).name("SUV").build();
        electricCategory = Category.builder().id(2L).name("Elétrico").build();

        branch = DealershipBranch.builder()
                .id(1L)
                .name("Matriz SP")
                .cnpj("11.222.333/0001-44")
                .city("São Paulo")
                .state("SP")
                .address("Av. das Nações Unidas, 14401")
                .maxDailyTestDrives(15)
                .build();

        vehicle = Vehicle.builder()
                .id(1L)
                .model("BMW iX3 Elétrico")
                .yearManufacture(java.time.LocalDate.now().getYear())
                .yearModel(java.time.LocalDate.now().getYear())
                .price(new BigDecimal("450000.00"))
                .mileage(8000)
                .color("Azul")
                .fuelType(FuelType.ELECTRIC)
                .transmission(TransmissionType.AUTOMATIC)
                .chassisNumber("9BWIX3EV2024001")
                .status(VehicleStatus.AVAILABLE)
                .brand(brandBmw)
                .category(electricCategory)
                .branch(branch)
                .images(new ArrayList<>())
                .features(List.of(
                        VehicleFeature.builder().id(1L).name("Teto Solar").build(),
                        VehicleFeature.builder().id(2L).name("Piloto Automático").build(),
                        VehicleFeature.builder().id(3L).name("Câmera 360").build()
                ))
                .build();
    }

    @Test
    @DisplayName("Deve calcular classificação INOVAÇÃO PREMIUM e alta elegibilidade para veículo elétrico recente")
    void calculateVehicleClassificationAndEligibility_ElectricVehicle_HighEligibility() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleEligibilityResponse response = vehicleService.calculateVehicleClassificationAndEligibility(1L);

        assertNotNull(response);
        assertEquals("INOVAÇÃO PREMIUM", response.classification());
        assertTrue(response.isEligibleForTestDrive());
        assertEquals(new BigDecimal("0.05"), response.depreciationRate());
        assertEquals(new BigDecimal("120.00"), response.estimatedInsurancePerDay());
        assertTrue(response.displayEligibilityScore() >= 100);
    }

    @Test
    @DisplayName("Deve calcular classificação SUV LUXO para SUV com preço > 150.000")
    void calculateVehicleClassificationAndEligibility_LuxurySuv_Success() {
        vehicle.setCategory(suvCategory);
        vehicle.setFuelType(FuelType.GASOLINE);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleEligibilityResponse response = vehicleService.calculateVehicleClassificationAndEligibility(1L);

        assertNotNull(response);
        assertEquals("SUV LUXO", response.classification());
        assertEquals(new BigDecimal("95.00"), response.estimatedInsurancePerDay());
        assertTrue(response.isEligibleForTestDrive());
    }

    @Test
    @DisplayName("Deve aplicar depreciação de 25% e penalidade de pontuação para alta quilometragem (>100.000 km)")
    void calculateVehicleClassificationAndEligibility_HighMileage_LowScore() {
        vehicle.setMileage(120000); // 120.000 km
        vehicle.setYearModel(2018); // > 5 anos
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleEligibilityResponse response = vehicleService.calculateVehicleClassificationAndEligibility(1L);

        assertNotNull(response);
        assertEquals(new BigDecimal("0.25"), response.depreciationRate());
        assertTrue(response.criteriaEvaluated().stream().anyMatch(c -> c.contains("Alta quilometragem ou idade avançada")));
    }

    @Test
    @DisplayName("Deve marcar como inelegível para test-drive se o status do veículo não for AVAILABLE")
    void calculateVehicleClassificationAndEligibility_NotAvailable_Ineligible() {
        vehicle.setStatus(VehicleStatus.MAINTENANCE);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleEligibilityResponse response = vehicleService.calculateVehicleClassificationAndEligibility(1L);

        assertNotNull(response);
        assertFalse(response.isEligibleForTestDrive());
        assertTrue(response.criteriaEvaluated().stream().anyMatch(c -> c.contains("Inelegível: Veículo não está com status AVAILABLE")));
    }

    @Test
    @DisplayName("Deve cadastrar um veículo com sucesso")
    void createVehicle_Success() {
        VehicleRequest req = new VehicleRequest(
                "Toyota Corolla", 2024, 2024, new BigDecimal("180000.00"), 5000,
                "Branco", FuelType.FLEX, TransmissionType.AUTOMATIC, "9BRCOROLLA202401",
                VehicleStatus.AVAILABLE, 1L, 1L, 1L, List.of(), List.of("http://foto.com/1.jpg")
        );

        when(vehicleRepository.existsByChassisNumber("9BRCOROLLA202401")).thenReturn(false);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brandBmw));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(suvCategory));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        Vehicle saved = Vehicle.builder()
                .id(10L)
                .model("Toyota Corolla")
                .yearManufacture(2024)
                .yearModel(2024)
                .price(new BigDecimal("180000.00"))
                .mileage(5000)
                .color("Branco")
                .fuelType(FuelType.FLEX)
                .transmission(TransmissionType.AUTOMATIC)
                .chassisNumber("9BRCOROLLA202401")
                .status(VehicleStatus.AVAILABLE)
                .brand(brandBmw)
                .category(suvCategory)
                .branch(branch)
                .build();
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(saved);

        VehicleResponse res = vehicleService.createVehicle(req, "admin@concessionaria.com");

        assertNotNull(res);
        assertEquals("Toyota Corolla", res.model());
        verify(vehicleRepository).save(any(Vehicle.class));
        verify(auditService).logAction(eq("CREATE_VEHICLE"), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar cadastrar veículo com chassi duplicado")
    void createVehicle_ThrowsWhenChassisExists() {
        VehicleRequest req = new VehicleRequest(
                "Toyota Corolla", 2024, 2024, new BigDecimal("180000.00"), 5000,
                "Branco", FuelType.FLEX, TransmissionType.AUTOMATIC, "9BRCOROLLA202401",
                VehicleStatus.AVAILABLE, 1L, 1L, 1L, null, null
        );

        when(vehicleRepository.existsByChassisNumber("9BRCOROLLA202401")).thenReturn(true);

        assertThrows(BusinessException.class, () -> vehicleService.createVehicle(req, "admin@concessionaria.com"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar excluir veículo em IN_TEST_DRIVE")
    void deleteVehicle_ThrowsWhenInTestDrive() {
        vehicle.setStatus(VehicleStatus.IN_TEST_DRIVE);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThrows(BusinessException.class, () -> vehicleService.deleteVehicle(1L, "admin@concessionaria.com"));
        verify(vehicleRepository, never()).delete(any(Vehicle.class));
    }

    @Test
    @DisplayName("Deve excluir veículo disponível com sucesso e registrar auditoria")
    void deleteVehicle_Success() {
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        vehicleService.deleteVehicle(1L, "admin@concessionaria.com");

        verify(vehicleRepository).delete(vehicle);
        verify(auditService).logAction(eq("DELETE_VEHICLE"), any(), any(), any(), any());
    }
}
