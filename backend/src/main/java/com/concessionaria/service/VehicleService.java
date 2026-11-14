package com.concessionaria.service;

import com.concessionaria.dto.*;
import com.concessionaria.entity.*;
import com.concessionaria.enums.FuelType;
import com.concessionaria.enums.VehicleStatus;
import com.concessionaria.exception.BusinessException;
import com.concessionaria.exception.ResourceNotFoundException;
import com.concessionaria.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final DealershipBranchRepository branchRepository;
    private final VehicleFeatureRepository featureRepository;
    private final AuditService auditService;

    /**
     * CODE SMELL INTENCIONAL: Complexidade Ciclomática (McCabe) &gt; 10 (Alvo: CC ~ 13).
     * Avalia a classificação comercial do veículo, calcula a taxa estimada de depreciação,
     * o valor da franquia diária de seguro para test-drive e a pontuação de elegibilidade de vitrine,
     * baseando-se em ano de modelo, quilometragem, tipo de combustível, categoria e lista de opcionais.
     * Toda a lógica reside em um único método com múltiplos branches propositais.
     */
    @Transactional(readOnly = true)
    public VehicleEligibilityResponse calculateVehicleClassificationAndEligibility(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com ID: " + vehicleId));

        int currentYear = LocalDate.now().getYear();
        int ageInYears = currentYear - vehicle.getYearModel();
        if (ageInYears < 0) {
            ageInYears = 0;
        }

        String classification;
        BigDecimal depreciationRate;
        BigDecimal baseInsuranceDay;
        int score = 100;
        List<String> criteria = new ArrayList<>();

        // 1. Classificação por Categoria e Preço (CC branch 1, 2, 3, 4)
        String catName = vehicle.getCategory().getName().toUpperCase();
        if (catName.contains("ELÉTRICO") || catName.contains("ELETRICO")) {
            classification = "INOVAÇÃO PREMIUM";
            baseInsuranceDay = new BigDecimal("120.00");
            score += 15;
            criteria.add("Veículo elétrico de alta tecnologia (+15 pts)");
        } else if (catName.contains("SUV") && vehicle.getPrice().compareTo(new BigDecimal("150000")) > 0) {
            classification = "SUV LUXO";
            baseInsuranceDay = new BigDecimal("95.00");
            score += 10;
            criteria.add("SUV de categoria luxo (+10 pts)");
        } else if (catName.contains("PICAPE") || catName.contains("PICKUP")) {
            classification = "UTILITÁRIO ROBUSTO";
            baseInsuranceDay = new BigDecimal("85.00");
            score += 5;
            criteria.add("Picape / Carga (+5 pts)");
        } else if (vehicle.getPrice().compareTo(new BigDecimal("200000")) > 0) {
            classification = "SUPER LUXO";
            baseInsuranceDay = new BigDecimal("150.00");
            score += 20;
            criteria.add("Veículo de alto padrão econômico (+20 pts)");
        } else {
            classification = "PASSEIO CONVENCIONAL";
            baseInsuranceDay = new BigDecimal("45.00");
            criteria.add("Classificação padrão de passeio");
        }

        // 2. Taxa de Depreciação por Idade e Quilometragem (CC branch 5, 6, 7)
        if (ageInYears <= 1 && vehicle.getMileage() < 15000) {
            depreciationRate = new BigDecimal("0.05"); // 5%
            score += 10;
            criteria.add("Semi-novo impecável (+10 pts)");
        } else if (ageInYears <= 3 && vehicle.getMileage() < 60000) {
            depreciationRate = new BigDecimal("0.12"); // 12%
            score += 5;
            criteria.add("Uso moderado dentro da média (+5 pts)");
        } else if (ageInYears > 5 || vehicle.getMileage() > 100000) {
            depreciationRate = new BigDecimal("0.25"); // 25%
            score -= 20;
            criteria.add("Alta quilometragem ou idade avançada (-20 pts)");
        } else {
            depreciationRate = new BigDecimal("0.18"); // 18%
            criteria.add("Depreciação padrão");
        }

        // 3. Ajuste por Tipo de Combustível (CC branch 8, 9)
        if (vehicle.getFuelType() == FuelType.DIESEL) {
            baseInsuranceDay = baseInsuranceDay.multiply(new BigDecimal("1.15"));
            criteria.add("Acréscimo de 15% seguro diesel");
        } else if (vehicle.getFuelType() == FuelType.ELECTRIC && ageInYears > 3) {
            baseInsuranceDay = baseInsuranceDay.multiply(new BigDecimal("1.25"));
            score -= 10;
            criteria.add("Acréscimo seguro bateria (>3 anos) (-10 pts)");
        }

        // 4. Bônus por Opcionais / Acessórios (CC branch 10, 11)
        int featureCount = vehicle.getFeatures() != null ? vehicle.getFeatures().size() : 0;
        if (featureCount >= 5) {
            score += 15;
            criteria.add("Pacote completo de opcionais (>=5 itens) (+15 pts)");
        } else if (featureCount >= 2) {
            score += 5;
            criteria.add("Pacote intermediário de opcionais (+5 pts)");
        } else {
            score -= 5;
            criteria.add("Poucos opcionais cadastrados (-5 pts)");
        }

        // 5. Decisão de Elegibilidade para Test-Drive Comercial (CC branch 12, 13)
        boolean isEligible;
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            isEligible = false;
            criteria.add("Inelegível: Veículo não está com status AVAILABLE");
        } else if (score < 60 || ageInYears > 10 || vehicle.getMileage() > 150000) {
            isEligible = false;
            criteria.add("Inelegível: Score insuficiente (<60) ou desgaste excessivo");
        } else {
            isEligible = true;
            criteria.add("Elegível para vitrine e agendamentos de test-drive");
        }

        BigDecimal calculatedInsurance = baseInsuranceDay.setScale(2, RoundingMode.HALF_UP);

        return new VehicleEligibilityResponse(
                vehicle.getId(),
                vehicle.getModel(),
                classification,
                depreciationRate,
                calculatedInsurance,
                isEligible,
                score,
                criteria
        );
    }

    /**
     * Cadastra um novo veículo no catálogo.
     * Complexidade Ciclomática (McCabe): 3 (CC &lt; 10 - Clean Code)
     */
    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request, String performedByEmail) {
        if (vehicleRepository.existsByChassisNumber(request.chassisNumber())) {
            throw new BusinessException("Número de chassi já cadastrado: " + request.chassisNumber());
        }

        Brand brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new ResourceNotFoundException("Marca não encontrada com ID: " + request.brandId()));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + request.categoryId()));
        DealershipBranch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Filial não encontrada com ID: " + request.branchId()));

        Vehicle vehicle = Vehicle.builder()
                .model(request.model())
                .yearManufacture(request.yearManufacture())
                .yearModel(request.yearModel())
                .price(request.price())
                .mileage(request.mileage())
                .color(request.color())
                .fuelType(request.fuelType())
                .transmission(request.transmission())
                .chassisNumber(request.chassisNumber())
                .status(request.status() != null ? request.status() : VehicleStatus.AVAILABLE)
                .brand(brand)
                .category(category)
                .branch(branch)
                .build();

        if (request.featureIds() != null && !request.featureIds().isEmpty()) {
            List<VehicleFeature> features = featureRepository.findAllById(request.featureIds());
            vehicle.setFeatures(features);
        }

        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            List<VehicleImage> images = new ArrayList<>();
            for (int i = 0; i < request.imageUrls().size(); i++) {
                images.add(VehicleImage.builder()
                        .url(request.imageUrls().get(i))
                        .isPrimary(i == 0)
                        .vehicle(vehicle)
                        .build());
            }
            vehicle.setImages(images);
        }

        Vehicle saved = vehicleRepository.save(vehicle);
        auditService.logAction("CREATE_VEHICLE", "Vehicle", saved.getId().toString(), performedByEmail, "Veículo cadastrado: " + saved.getModel());
        return toDto(saved);
    }

    /**
     * Busca um veículo por ID detalhando opcionais e imagens.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com ID: " + id));
        return toDto(vehicle);
    }

    /**
     * Pesquisa e filtra veículos por múltiplos parâmetros dinâmicos.
     * Complexidade Ciclomática (McCabe): 5 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> searchVehicles(VehicleFilterRequest filter) {
        Specification<Vehicle> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.brandId() != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), filter.brandId()));
            }
            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
            }
            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }
            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }
            if (filter.fuelType() != null) {
                predicates.add(cb.equal(root.get("fuelType"), filter.fuelType()));
            }
            if (filter.transmission() != null) {
                predicates.add(cb.equal(root.get("transmission"), filter.transmission()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.searchTerm() != null && !filter.searchTerm().isBlank()) {
                String search = "%" + filter.searchTerm().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("model")), search),
                        cb.like(cb.lower(root.get("brand").get("name")), search)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return vehicleRepository.findAll(spec)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Atualiza os dados cadastrais de um veículo existente.
     * Complexidade Ciclomática (McCabe): 3 (CC &lt; 10 - Clean Code)
     */
    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleRequest request, String performedByEmail) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com ID: " + id));

        if (!vehicle.getChassisNumber().equals(request.chassisNumber()) &&
                vehicleRepository.existsByChassisNumber(request.chassisNumber())) {
            throw new BusinessException("Número de chassi já cadastrado em outro veículo: " + request.chassisNumber());
        }

        Brand brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new ResourceNotFoundException("Marca não encontrada com ID: " + request.brandId()));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + request.categoryId()));
        DealershipBranch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Filial não encontrada com ID: " + request.branchId()));

        vehicle.setModel(request.model());
        vehicle.setYearManufacture(request.yearManufacture());
        vehicle.setYearModel(request.yearModel());
        vehicle.setPrice(request.price());
        vehicle.setMileage(request.mileage());
        vehicle.setColor(request.color());
        vehicle.setFuelType(request.fuelType());
        vehicle.setTransmission(request.transmission());
        vehicle.setChassisNumber(request.chassisNumber());
        if (request.status() != null) {
            vehicle.setStatus(request.status());
        }
        vehicle.setBrand(brand);
        vehicle.setCategory(category);
        vehicle.setBranch(branch);

        if (request.featureIds() != null) {
            List<VehicleFeature> features = featureRepository.findAllById(request.featureIds());
            vehicle.setFeatures(features);
        }

        Vehicle updated = vehicleRepository.save(vehicle);
        auditService.logAction("UPDATE_VEHICLE", "Vehicle", updated.getId().toString(), performedByEmail, "Veículo atualizado: " + updated.getModel());
        return toDto(updated);
    }

    /**
     * Remove um veículo do catálogo da concessionária.
     * Complexidade Ciclomática (McCabe): 2 (CC &lt; 10 - Clean Code)
     */
    @Transactional
    public void deleteVehicle(Long id, String performedByEmail) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com ID: " + id));

        if (vehicle.getStatus() == VehicleStatus.IN_TEST_DRIVE) {
            throw new BusinessException("Não é possível excluir um veículo que está atualmente em test-drive");
        }

        vehicleRepository.delete(vehicle);
        auditService.logAction("DELETE_VEHICLE", "Vehicle", id.toString(), performedByEmail, "Veículo excluído: " + vehicle.getModel());
    }

    public VehicleResponse toDto(Vehicle vehicle) {
        List<VehicleImageDto> images = vehicle.getImages() != null ? vehicle.getImages().stream()
                .map(img -> new VehicleImageDto(img.getId(), img.getUrl(), img.getIsPrimary()))
                .toList() : List.of();

        List<VehicleFeatureDto> features = vehicle.getFeatures() != null ? vehicle.getFeatures().stream()
                .map(f -> new VehicleFeatureDto(f.getId(), f.getName(), f.getDescription()))
                .toList() : List.of();

        BrandResponse brandDto = new BrandResponse(
                vehicle.getBrand().getId(),
                vehicle.getBrand().getName(),
                vehicle.getBrand().getCountryOfOrigin(),
                vehicle.getBrand().getActive()
        );

        CategoryResponse categoryDto = new CategoryResponse(
                vehicle.getCategory().getId(),
                vehicle.getCategory().getName(),
                vehicle.getCategory().getDescription()
        );

        BranchResponse branchDto = new BranchResponse(
                vehicle.getBranch().getId(),
                vehicle.getBranch().getName(),
                vehicle.getBranch().getCnpj(),
                vehicle.getBranch().getCity(),
                vehicle.getBranch().getState(),
                vehicle.getBranch().getAddress(),
                vehicle.getBranch().getMaxDailyTestDrives()
        );

        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getModel(),
                vehicle.getYearManufacture(),
                vehicle.getYearModel(),
                vehicle.getPrice(),
                vehicle.getMileage(),
                vehicle.getColor(),
                vehicle.getFuelType(),
                vehicle.getTransmission(),
                vehicle.getChassisNumber(),
                vehicle.getStatus(),
                brandDto,
                categoryDto,
                branchDto,
                images,
                features
        );
    }
}

