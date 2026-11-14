package com.concessionaria.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BranchRequest(
        @NotBlank(message = "Nome da filial é obrigatório")
        String name,

        @NotBlank(message = "CNPJ é obrigatório")
        String cnpj,

        @NotBlank(message = "Cidade é obrigatória")
        String city,

        @NotBlank(message = "Estado (UF) é obrigatório")
        @Size(min = 2, max = 2, message = "UF deve ter 2 caracteres")
        String state,

        @NotBlank(message = "Endereço é obrigatório")
        String address,

        @NotNull(message = "Capacidade diária de test-drives é obrigatória")
        @Min(value = 1, message = "Capacidade diária deve ser no mínimo 1")
        Integer maxDailyTestDrives
) {}

