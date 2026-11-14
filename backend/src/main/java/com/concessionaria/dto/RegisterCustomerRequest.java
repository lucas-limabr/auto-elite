package com.concessionaria.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterCustomerRequest(
        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password,

        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "Formato de CPF inválido")
        String cpf,

        @NotBlank(message = "Telefone é obrigatório")
        String phone,

        @NotBlank(message = "Número da CNH é obrigatório")
        String cnhNumber,

        @NotNull(message = "Data de validade da CNH é obrigatória")
        @Future(message = "CNH deve estar válida")
        LocalDate cnhExpirationDate,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve ser no passado")
        LocalDate birthDate
) {}

