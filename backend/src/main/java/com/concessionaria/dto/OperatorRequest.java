package com.concessionaria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record OperatorRequest(
        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password,

        @NotBlank(message = "Matrícula é obrigatória")
        String registrationCode,

        @NotNull(message = "Data de contratação é obrigatória")
        LocalDate hireDate,

        @NotBlank(message = "Departamento é obrigatório")
        String department,

        @NotNull(message = "ID da filial é obrigatório")
        Long branchId
) {}

