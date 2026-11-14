package com.concessionaria.dto;

import java.time.LocalDate;

public record OperatorResponse(
        Long id,
        Long userId,
        String name,
        String email,
        String registrationCode,
        LocalDate hireDate,
        String department,
        Long branchId,
        String branchName,
        Boolean active
) {}

