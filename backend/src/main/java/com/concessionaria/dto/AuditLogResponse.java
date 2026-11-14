package com.concessionaria.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String action,
        String entityName,
        String entityId,
        String performedByEmail,
        LocalDateTime timestamp,
        String details
) {}

