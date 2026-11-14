package com.concessionaria.dto;

import com.concessionaria.enums.FuelType;
import com.concessionaria.enums.TransmissionType;
import com.concessionaria.enums.VehicleStatus;

import java.math.BigDecimal;

public record VehicleFilterRequest(
        Long brandId,
        Long categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        FuelType fuelType,
        TransmissionType transmission,
        VehicleStatus status,
        String searchTerm
) {}

