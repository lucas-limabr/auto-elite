package com.concessionaria.service;

import com.concessionaria.dto.AuditLogResponse;
import com.concessionaria.entity.AuditLog;
import com.concessionaria.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Registra uma nova ação de auditoria no sistema.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional
    public AuditLogResponse logAction(String action, String entityName, String entityId, String performedByEmail, String details) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .performedByEmail(performedByEmail != null ? performedByEmail : "SYSTEM")
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
        AuditLog saved = auditLogRepository.save(log);
        return toDto(saved);
    }

    /**
     * Retorna todos os registros de auditoria ordenados do mais recente ao mais antigo.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Retorna registros de auditoria filtrados pelo nome da entidade.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByEntity(String entityName) {
        return auditLogRepository.findByEntityNameOrderByTimestampDesc(entityName)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private AuditLogResponse toDto(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getEntityName(),
                log.getEntityId(),
                log.getPerformedByEmail(),
                log.getTimestamp(),
                log.getDetails()
        );
    }
}

