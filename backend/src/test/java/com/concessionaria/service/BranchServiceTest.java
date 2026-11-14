package com.concessionaria.service;

import com.concessionaria.dto.BranchRequest;
import com.concessionaria.dto.BranchResponse;
import com.concessionaria.entity.DealershipBranch;
import com.concessionaria.exception.BusinessException;
import com.concessionaria.exception.ResourceNotFoundException;
import com.concessionaria.repository.DealershipBranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private DealershipBranchRepository branchRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private BranchService branchService;

    private BranchRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new BranchRequest(
                "Filial Campinas",
                "33.444.555/0001-66",
                "Campinas",
                "SP",
                "Av. Brasil, 500",
                12
        );
    }

    @Test
    @DisplayName("Deve criar filial com sucesso quando dados válidos")
    void createBranch_Success() {
        when(branchRepository.existsByCnpj(anyString())).thenReturn(false);

        DealershipBranch saved = DealershipBranch.builder()
                .id(1L)
                .name(validRequest.name())
                .cnpj(validRequest.cnpj())
                .city(validRequest.city())
                .state(validRequest.state())
                .address(validRequest.address())
                .maxDailyTestDrives(validRequest.maxDailyTestDrives())
                .build();
        when(branchRepository.save(any(DealershipBranch.class))).thenReturn(saved);

        BranchResponse res = branchService.createBranch(validRequest, "admin@concessionaria.com");

        assertNotNull(res);
        assertEquals("Filial Campinas", res.name());
        assertEquals("33.444.555/0001-66", res.cnpj());
        verify(branchRepository).save(any(DealershipBranch.class));
        verify(auditService).logAction(eq("CREATE_BRANCH"), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar criar filial com CNPJ duplicado")
    void createBranch_ThrowsWhenCnpjExists() {
        when(branchRepository.existsByCnpj("33.444.555/0001-66")).thenReturn(true);

        assertThrows(BusinessException.class, () ->
                branchService.createBranch(validRequest, "admin@concessionaria.com"));
    }

    @Test
    @DisplayName("Deve retornar filial por ID com sucesso")
    void getBranchById_Success() {
        DealershipBranch branch = DealershipBranch.builder()
                .id(1L)
                .name("Matriz SP")
                .cnpj("11.222.333/0001-44")
                .city("São Paulo")
                .state("SP")
                .address("Av. Paulista")
                .maxDailyTestDrives(15)
                .build();
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        BranchResponse res = branchService.getBranchById(1L);
        assertNotNull(res);
        assertEquals("Matriz SP", res.name());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando filial não for encontrada")
    void getBranchById_ThrowsWhenNotFound() {
        when(branchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> branchService.getBranchById(99L));
    }

    @Test
    @DisplayName("Deve atualizar filial com sucesso")
    void updateBranch_Success() {
        DealershipBranch existing = DealershipBranch.builder()
                .id(1L)
                .name("Filial Antiga")
                .cnpj("33.444.555/0001-66")
                .city("Campinas")
                .state("SP")
                .address("Endereço Velho")
                .maxDailyTestDrives(5)
                .build();

        when(branchRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(branchRepository.save(any(DealershipBranch.class))).thenAnswer(i -> i.getArgument(0));

        BranchResponse res = branchService.updateBranch(1L, validRequest, "admin@concessionaria.com");

        assertNotNull(res);
        assertEquals("Filial Campinas", res.name());
        assertEquals(12, res.maxDailyTestDrives());
        verify(branchRepository).save(existing);
        verify(auditService).logAction(eq("UPDATE_BRANCH"), any(), any(), any(), any());
    }
}

