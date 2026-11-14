package com.concessionaria.dto;

public record BranchResponse(
        Long id,
        String name,
        String cnpj,
        String city,
        String state,
        String address,
        Integer maxDailyTestDrives
) {}

