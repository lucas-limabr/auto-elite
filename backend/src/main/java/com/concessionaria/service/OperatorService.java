package com.concessionaria.service;

import com.concessionaria.dto.OperatorRequest;
import com.concessionaria.dto.OperatorResponse;
import com.concessionaria.entity.DealershipBranch;
import com.concessionaria.entity.OperatorProfile;
import com.concessionaria.entity.User;
import com.concessionaria.enums.Role;
import com.concessionaria.exception.BusinessException;
import com.concessionaria.exception.ResourceNotFoundException;
import com.concessionaria.repository.DealershipBranchRepository;
import com.concessionaria.repository.OperatorProfileRepository;
import com.concessionaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperatorService {

    private final UserRepository userRepository;
    private final OperatorProfileRepository operatorProfileRepository;
    private final DealershipBranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    /**
     * Cadastra um novo operador/funcionário vinculado a uma filial.
     * Complexidade Ciclomática (McCabe): 3 (CC &lt; 10 - Clean Code)
     */
    @Transactional
    public OperatorResponse createOperator(OperatorRequest request, String performedByEmail) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado: " + request.email());
        }
        if (operatorProfileRepository.existsByRegistrationCode(request.registrationCode())) {
            throw new BusinessException("Matrícula de operador já existente: " + request.registrationCode());
        }

        DealershipBranch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Filial não encontrada com ID: " + request.branchId()));

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.OPERATOR)
                .active(true)
                .build();
        User savedUser = userRepository.save(user);

        OperatorProfile profile = OperatorProfile.builder()
                .user(savedUser)
                .registrationCode(request.registrationCode())
                .hireDate(request.hireDate())
                .department(request.department())
                .dealershipBranch(branch)
                .build();
        OperatorProfile savedProfile = operatorProfileRepository.save(profile);

        auditService.logAction("CREATE_OPERATOR", "OperatorProfile", savedProfile.getId().toString(), performedByEmail, "Operador criado: " + savedUser.getEmail());

        return toDto(savedProfile);
    }

    /**
     * Retorna a lista de todos os operadores cadastrados.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public List<OperatorResponse> getAllOperators() {
        return operatorProfileRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Busca um operador pelo seu ID de perfil.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public OperatorResponse getOperatorById(Long id) {
        OperatorProfile profile = operatorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operador não encontrado com ID: " + id));
        return toDto(profile);
    }

    /**
     * Ativa ou desativa o acesso de um operador no sistema.
     * Complexidade Ciclomática (McCabe): 2 (CC &lt; 10 - Clean Code)
     */
    @Transactional
    public OperatorResponse toggleOperatorStatus(Long id, String performedByEmail) {
        OperatorProfile profile = operatorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operador não encontrado com ID: " + id));

        User user = profile.getUser();
        boolean newStatus = !Boolean.TRUE.equals(user.getActive());
        user.setActive(newStatus);
        userRepository.save(user);

        auditService.logAction("TOGGLE_OPERATOR_STATUS", "OperatorProfile", id.toString(), performedByEmail, "Operador status alterado para: " + (newStatus ? "ATIVO" : "INATIVO"));

        return toDto(profile);
    }

    public OperatorResponse toDto(OperatorProfile profile) {
        return new OperatorResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getUser().getEmail(),
                profile.getRegistrationCode(),
                profile.getHireDate(),
                profile.getDepartment(),
                profile.getDealershipBranch().getId(),
                profile.getDealershipBranch().getName(),
                profile.getUser().getActive()
        );
    }
}

