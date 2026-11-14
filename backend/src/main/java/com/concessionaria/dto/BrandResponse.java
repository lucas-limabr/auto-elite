package com.concessionaria.dto;

public record BrandResponse(
        Long id,
        String name,
        String countryOfOrigin,
        Boolean active
) {}

