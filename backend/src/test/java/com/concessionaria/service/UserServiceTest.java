package com.concessionaria.service;

import com.concessionaria.dto.CustomerResponse;
import com.concessionaria.dto.RegisterCustomerRequest;
import com.concessionaria.entity.CustomerProfile;
import com.concessionaria.entity.User;
import com.concessionaria.enums.Role;
import com.concessionaria.exception.BusinessException;
import com.concessionaria.exception.ResourceNotFoundException;
import com.concessionaria.repository.CustomerProfileRepository;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserService userService;

    private RegisterCustomerRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterCustomerRequest(
                "Mariana Lima",
                "mariana@email.com",
                "senha123",
                "111.222.333-44",
                "(11) 99999-8888",
                "98765432100",
                LocalDate.now().plusYears(3),
                LocalDate.now().minusYears(25) // 25 anos
        );
    }

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso quando dados válidos")
    void registerCustomer_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerProfileRepository.existsByCpf(anyString())).thenReturn(false);
        when(customerProfileRepository.existsByCnhNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(1L)
                .name(validRequest.name())
                .email(validRequest.email())
                .password("encodedPassword")
                .role(Role.CUSTOMER)
                .active(true)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        CustomerProfile savedProfile = CustomerProfile.builder()
                .id(1L)
                .user(savedUser)
                .cpf(validRequest.cpf())
                .phone(validRequest.phone())
                .cnhNumber(validRequest.cnhNumber())
                .cnhExpirationDate(validRequest.cnhExpirationDate())
                .birthDate(validRequest.birthDate())
                .build();
        when(customerProfileRepository.save(any(CustomerProfile.class))).thenReturn(savedProfile);

        CustomerResponse response = userService.registerCustomer(validRequest);

        assertNotNull(response);
        assertEquals("Mariana Lima", response.name());
        assertEquals("mariana@email.com", response.email());
        verify(userRepository).save(any(User.class));
        verify(customerProfileRepository).save(any(CustomerProfile.class));
        verify(auditService).logAction(eq("REGISTER_CUSTOMER"), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando email já existir")
    void registerCustomer_ThrowsWhenEmailExists() {
        when(userRepository.existsByEmail("mariana@email.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.registerCustomer(validRequest));
        assertTrue(ex.getMessage().contains("Email já cadastrado"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando CPF já existir")
    void registerCustomer_ThrowsWhenCpfExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerProfileRepository.existsByCpf(anyString())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.registerCustomer(validRequest));
        assertTrue(ex.getMessage().contains("CPF já cadastrado"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando CNH já existir")
    void registerCustomer_ThrowsWhenCnhExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerProfileRepository.existsByCpf(anyString())).thenReturn(false);
        when(customerProfileRepository.existsByCnhNumber(anyString())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.registerCustomer(validRequest));
        assertTrue(ex.getMessage().contains("CNH já cadastrado"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException se cliente for menor de 18 anos")
    void registerCustomer_ThrowsWhenUnderage() {
        RegisterCustomerRequest minorRequest = new RegisterCustomerRequest(
                "Menor de Idade", "menor@email.com", "senha123", "000.111.222-33",
                "(11) 98765-4321", "12312312312", LocalDate.now().plusYears(1),
                LocalDate.now().minusYears(16) // 16 anos
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerProfileRepository.existsByCpf(anyString())).thenReturn(false);
        when(customerProfileRepository.existsByCnhNumber(anyString())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.registerCustomer(minorRequest));
        assertTrue(ex.getMessage().contains("18 anos"));
    }

    @Test
    @DisplayName("Deve retornar perfil de cliente por ID de usuário")
    void getCustomerProfileByUserId_Success() {
        User user = User.builder().id(5L).name("João").email("joao@email.com").build();
        CustomerProfile profile = CustomerProfile.builder()
                .id(1L)
                .user(user)
                .cpf("111.111.111-11")
                .phone("(11) 91111-1111")
                .cnhNumber("11111111111")
                .birthDate(LocalDate.of(1990, 1, 1))
                .cnhExpirationDate(LocalDate.of(2028, 1, 1))
                .build();

        when(customerProfileRepository.findByUserId(5L)).thenReturn(Optional.of(profile));

        CustomerResponse res = userService.getCustomerProfileByUserId(5L);
        assertNotNull(res);
        assertEquals("João", res.name());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando perfil do cliente não existir")
    void getCustomerProfileByUserId_ThrowsWhenNotFound() {
        when(customerProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getCustomerProfileByUserId(99L));
    }
}

