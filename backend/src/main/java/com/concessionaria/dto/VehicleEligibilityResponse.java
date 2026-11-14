package com.concessionaria.dto;

import java.math.BigDecimal;
import java.util.List;

public record VehicleEligibilityResponse(
        Long vehicleId,
        String model,
        String classification,
        BigDecimal depreciationRate,
        BigDecimal estimatedInsurancePerDay,
        Boolean isEligibleForTestDrive,
        Integer displayEligibilityScore,
        List<String> criteriaEvaluated
) {}

