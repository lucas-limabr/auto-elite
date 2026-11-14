package com.concessionaria.service;

import com.concessionaria.dto.CategoryRequest;
import com.concessionaria.dto.CategoryResponse;
import com.concessionaria.entity.Category;
import com.concessionaria.exception.BusinessException;
import com.concessionaria.exception.ResourceNotFoundException;
import com.concessionaria.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditService auditService;

    /**
     * Cadastra uma nova categoria de veículo.
     * Complexidade Ciclomática (McCabe): 2 (CC &lt; 10 - Clean Code)
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, String performedByEmail) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Categoria já cadastrada com o nome: " + request.name());
        }

        Category category = Category.builder()
                .name(request.name().trim())
                .description(request.description())
                .build();

        Category saved = categoryRepository.save(category);
        auditService.logAction("CREATE_CATEGORY", "Category", saved.getId().toString(), performedByEmail, "Categoria criada: " + saved.getName());
        return toDto(saved);
    }

    /**
     * Lista todas as categorias cadastradas.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Busca uma categoria por ID.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + id));
        return toDto(category);
    }

    public CategoryResponse toDto(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}

