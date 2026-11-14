package com.concessionaria.dto;

import com.concessionaria.enums.FuelType;
import com.concessionaria.enums.TransmissionType;
import com.concessionaria.enums.VehicleStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record VehicleRequest(
        @NotBlank(message = "Modelo é obrigatório")
        String model,

        @NotNull(message = "Ano de fabricação é obrigatório")
        @Min(value = 1900, message = "Ano de fabricação inválido")
        Integer yearManufacture,

        @NotNull(message = "Ano do modelo é obrigatório")
        @Min(value = 1900, message = "Ano do modelo inválido")
        Integer yearModel,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço deve ser positivo")
        BigDecimal price,

        @NotNull(message = "Quilometragem é obrigatória")
        @Min(value = 0, message = "Quilometragem não pode ser negativa")
        Integer mileage,

        @NotBlank(message = "Cor é obrigatória")
        String color,

        @NotNull(message = "Tipo de combustível é obrigatório")
        FuelType fuelType,

        @NotNull(message = "Câmbio é obrigatório")
        TransmissionType transmission,

        @NotBlank(message = "Número do chassi é obrigatório")
        String chassisNumber,

        VehicleStatus status,

        @NotNull(message = "Marca é obrigatória")
        Long brandId,

        @NotNull(message = "Categoria é obrigatória")
        Long categoryId,

        @NotNull(message = "Filial é obrigatória")
        Long branchId,

        List<Long> featureIds,

        List<String> imageUrls
) {}

