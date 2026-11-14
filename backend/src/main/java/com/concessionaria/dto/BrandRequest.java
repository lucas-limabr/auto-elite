package com.concessionaria.dto;

import jakarta.validation.constraints.NotBlank;

public record BrandRequest(
        @NotBlank(message = "Nome da marca é obrigatório")
        String name,

        @NotBlank(message = "País de origem é obrigatório")
        String countryOfOrigin,

        Boolean active
) {}

