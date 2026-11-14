package com.concessionaria.service;

import com.concessionaria.dto.BrandRequest;
import com.concessionaria.dto.BrandResponse;
import com.concessionaria.entity.Brand;
import com.concessionaria.exception.BusinessException;
import com.concessionaria.exception.ResourceNotFoundException;
import com.concessionaria.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final AuditService auditService;

    /**
     * Cadastra uma nova marca de veículos.
     * Complexidade Ciclomática (McCabe): 2 (CC &lt; 10 - Clean Code)
     */
    @Transactional
    public BrandResponse createBrand(BrandRequest request, String performedByEmail) {
        if (brandRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Marca já cadastrada com o nome: " + request.name());
        }

        Brand brand = Brand.builder()
                .name(request.name().trim())
                .countryOfOrigin(request.countryOfOrigin().trim())
                .active(request.active() != null ? request.active() : true)
                .build();

        Brand saved = brandRepository.save(brand);
        auditService.logAction("CREATE_BRAND", "Brand", saved.getId().toString(), performedByEmail, "Marca criada: " + saved.getName());
        return toDto(saved);
    }

    /**
     * Lista todas as marcas ativas.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Busca uma marca por ID.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca não encontrada com ID: " + id));
        return toDto(brand);
    }

    public BrandResponse toDto(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getCountryOfOrigin(),
                brand.getActive()
        );
    }
}

