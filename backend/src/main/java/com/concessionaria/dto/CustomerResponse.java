package com.concessionaria.dto;

import java.time.LocalDate;

public record CustomerResponse(
        Long id,
        Long userId,
        String name,
        String email,
        String cpf,
        String phone,
        String cnhNumber,
        LocalDate cnhExpirationDate,
        LocalDate birthDate
) {}

