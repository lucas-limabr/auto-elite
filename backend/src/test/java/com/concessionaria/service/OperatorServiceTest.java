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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperatorServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OperatorProfileRepository operatorProfileRepository;

    @Mock
    private DealershipBranchRepository branchRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private OperatorService operatorService;

    private DealershipBranch branch;
    private OperatorRequest validRequest;

    @BeforeEach
    void setUp() {
        branch = DealershipBranch.builder()
                .id(1L)
                .name("Matriz SP")
                .cnpj("11.222.333/0001-44")
                .city("São Paulo")
                .state("SP")
                .address("Av. Paulista")
                .maxDailyTestDrives(10)
                .build();

        validRequest = new OperatorRequest(
                "Fernando Costa",
                "fernando@concessionaria.com",
                "senha123",
                "OP-005",
                LocalDate.of(2023, 1, 15),
                "Vendas",
                1L
        );
    }

    @Test
    @DisplayName("Deve cadastrar operador vinculado à filial com sucesso")
    void createOperator_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(operatorProfileRepository.existsByRegistrationCode(anyString())).thenReturn(false);
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(10L)
                .name(validRequest.name())
                .email(validRequest.email())
                .password("encodedPassword")
                .role(Role.OPERATOR)
                .active(true)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        OperatorProfile savedProfile = OperatorProfile.builder()
                .id(1L)
                .user(savedUser)
                .registrationCode(validRequest.registrationCode())
                .hireDate(validRequest.hireDate())
                .department(validRequest.department())
                .dealershipBranch(branch)
                .build();
        when(operatorProfileRepository.save(any(OperatorProfile.class))).thenReturn(savedProfile);

        OperatorResponse res = operatorService.createOperator(validRequest, "admin@concessionaria.com");

        assertNotNull(res);
        assertEquals("Fernando Costa", res.name());
        assertEquals("OP-005", res.registrationCode());
        verify(userRepository).save(any(User.class));
        verify(operatorProfileRepository).save(any(OperatorProfile.class));
        verify(auditService).logAction(eq("CREATE_OPERATOR"), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar cadastrar operador com email já existente")
    void createOperator_ThrowsWhenEmailExists() {
        when(userRepository.existsByEmail("fernando@concessionaria.com")).thenReturn(true);

        assertThrows(BusinessException.class, () ->
                operatorService.createOperator(validRequest, "admin@concessionaria.com"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando matrícula de operador já existir")
    void createOperator_ThrowsWhenRegistrationCodeExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(operatorProfileRepository.existsByRegistrationCode("OP-005")).thenReturn(true);

        assertThrows(BusinessException.class, () ->
                operatorService.createOperator(validRequest, "admin@concessionaria.com"));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando a filial não for encontrada")
    void createOperator_ThrowsWhenBranchNotFound() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(operatorProfileRepository.existsByRegistrationCode(anyString())).thenReturn(false);
        when(branchRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                operatorService.createOperator(validRequest, "admin@concessionaria.com"));
    }

    @Test
    @DisplayName("Deve alternar status ativo/inativo do operador com sucesso")
    void toggleOperatorStatus_Success() {
        User user = User.builder().id(10L).email("op@email.com").active(true).name("Op 1").build();
        OperatorProfile profile = OperatorProfile.builder()
                .id(1L)
                .user(user)
                .registrationCode("OP-001")
                .hireDate(LocalDate.now())
                .department("Vendas")
                .dealershipBranch(branch)
                .build();

        when(operatorProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        OperatorResponse res = operatorService.toggleOperatorStatus(1L, "admin@concessionaria.com");

        assertNotNull(res);
        assertFalse(res.active());
        verify(userRepository).save(user);
        verify(auditService).logAction(eq("TOGGLE_OPERATOR_STATUS"), any(), any(), any(), any());
    }
}

