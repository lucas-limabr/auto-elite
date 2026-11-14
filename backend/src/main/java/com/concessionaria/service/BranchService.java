package com.concessionaria.service;

import com.concessionaria.dto.BranchRequest;
import com.concessionaria.dto.BranchResponse;
import com.concessionaria.entity.DealershipBranch;
import com.concessionaria.exception.BusinessException;
import com.concessionaria.exception.ResourceNotFoundException;
import com.concessionaria.repository.DealershipBranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final DealershipBranchRepository branchRepository;
    private final AuditService auditService;

    /**
     * Cadastra uma nova filial da concessionária.
     * Complexidade Ciclomática (McCabe): 2 (CC &lt; 10 - Clean Code)
     */
    @Transactional
    public BranchResponse createBranch(BranchRequest request, String performedByEmail) {
        if (branchRepository.existsByCnpj(request.cnpj())) {
            throw new BusinessException("Já existe uma filial cadastrada com o CNPJ informado: " + request.cnpj());
        }

        DealershipBranch branch = DealershipBranch.builder()
                .name(request.name())
                .cnpj(request.cnpj())
                .city(request.city())
                .state(request.state().toUpperCase())
                .address(request.address())
                .maxDailyTestDrives(request.maxDailyTestDrives())
                .build();

        DealershipBranch saved = branchRepository.save(branch);
        auditService.logAction("CREATE_BRANCH", "DealershipBranch", saved.getId().toString(), performedByEmail, "Filial criada: " + saved.getName());
        return toDto(saved);
    }

    /**
     * Busca uma filial por ID.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        DealershipBranch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filial não encontrada com ID: " + id));
        return toDto(branch);
    }

    /**
     * Lista todas as filiais cadastradas.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Atualiza os dados de uma filial existente.
     * Complexidade Ciclomática (McCabe): 2 (CC &lt; 10 - Clean Code)
     */
    @Transactional
    public BranchResponse updateBranch(Long id, BranchRequest request, String performedByEmail) {
        DealershipBranch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filial não encontrada com ID: " + id));

        if (!branch.getCnpj().equals(request.cnpj()) && branchRepository.existsByCnpj(request.cnpj())) {
            throw new BusinessException("CNPJ já utilizado por outra filial: " + request.cnpj());
        }

        branch.setName(request.name());
        branch.setCnpj(request.cnpj());
        branch.setCity(request.city());
        branch.setState(request.state().toUpperCase());
        branch.setAddress(request.address());
        branch.setMaxDailyTestDrives(request.maxDailyTestDrives());

        DealershipBranch updated = branchRepository.save(branch);
        auditService.logAction("UPDATE_BRANCH", "DealershipBranch", updated.getId().toString(), performedByEmail, "Filial atualizada: " + updated.getName());
        return toDto(updated);
    }

    public BranchResponse toDto(DealershipBranch branch) {
        return new BranchResponse(
                branch.getId(),
                branch.getName(),
                branch.getCnpj(),
                branch.getCity(),
                branch.getState(),
                branch.getAddress(),
                branch.getMaxDailyTestDrives()
        );
    }
}

