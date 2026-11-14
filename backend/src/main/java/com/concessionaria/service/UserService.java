package com.concessionaria.service;

import com.concessionaria.dto.CustomerResponse;
import com.concessionaria.dto.RegisterCustomerRequest;
import com.concessionaria.dto.UserResponse;
import com.concessionaria.entity.CustomerProfile;
import com.concessionaria.entity.User;
import com.concessionaria.enums.Role;
import com.concessionaria.exception.BusinessException;
import com.concessionaria.exception.ResourceNotFoundException;
import com.concessionaria.repository.CustomerProfileRepository;
import com.concessionaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    /**
     * Registra um novo cliente com suas informações de CNH e perfil.
     * Complexidade Ciclomática (McCabe): 4 (CC &lt; 10 - Clean Code)
     */
    @Transactional
    public CustomerResponse registerCustomer(RegisterCustomerRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado: " + request.email());
        }
        if (customerProfileRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("CPF já cadastrado: " + request.cpf());
        }
        if (customerProfileRepository.existsByCnhNumber(request.cnhNumber())) {
            throw new BusinessException("Número de CNH já cadastrado: " + request.cnhNumber());
        }

        // Validação de maioridade (18 anos)
        int age = Period.between(request.birthDate(), LocalDate.now()).getYears();
        if (age < 18) {
            throw new BusinessException("O cliente deve ter pelo menos 18 anos de idade");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .active(true)
                .build();
        User savedUser = userRepository.save(user);

        CustomerProfile profile = CustomerProfile.builder()
                .user(savedUser)
                .cpf(request.cpf())
                .phone(request.phone())
                .cnhNumber(request.cnhNumber())
                .cnhExpirationDate(request.cnhExpirationDate())
                .birthDate(request.birthDate())
                .build();
        CustomerProfile savedProfile = customerProfileRepository.save(profile);

        auditService.logAction("REGISTER_CUSTOMER", "User", savedUser.getId().toString(), savedUser.getEmail(), "Cadastro de novo cliente realizado com sucesso");

        return toCustomerDto(savedProfile);
    }

    /**
     * Busca o perfil de cliente vinculado a um ID de usuário.
     * Complexidade Ciclomática (McCabe): 2 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerProfileByUserId(Long userId) {
        CustomerProfile profile = customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de cliente não encontrado para o usuário: " + userId));
        return toCustomerDto(profile);
    }

    /**
     * Busca um usuário por ID.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));
        return toUserDto(user);
    }

    public CustomerResponse toCustomerDto(CustomerProfile profile) {
        return new CustomerResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getUser().getEmail(),
                profile.getCpf(),
                profile.getPhone(),
                profile.getCnhNumber(),
                profile.getCnhExpirationDate(),
                profile.getBirthDate()
        );
    }

    public UserResponse toUserDto(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt()
        );
    }
}

