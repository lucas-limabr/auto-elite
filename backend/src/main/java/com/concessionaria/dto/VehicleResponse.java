package com.concessionaria.dto;

import com.concessionaria.enums.FuelType;
import com.concessionaria.enums.TransmissionType;
import com.concessionaria.enums.VehicleStatus;

import java.math.BigDecimal;
import java.util.List;

public record VehicleResponse(
        Long id,
        String model,
        Integer yearManufacture,
        Integer yearModel,
        BigDecimal price,
        Integer mileage,
        String color,
        FuelType fuelType,
        TransmissionType transmission,
        String chassisNumber,
        VehicleStatus status,
        BrandResponse brand,
        CategoryResponse category,
        BranchResponse branch,
        List<VehicleImageDto> images,
        List<VehicleFeatureDto> features
) {}

