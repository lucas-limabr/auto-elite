package com.concessionaria.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FeedbackRequest(
        @NotNull(message = "Avaliação é obrigatória")
        @Min(value = 1, message = "Avaliação mínima é 1")
        @Max(value = 5, message = "Avaliação máxima é 5")
        Integer rating,

        String comments,

        @NotNull(message = "Interesse de compra é obrigatório")
        Boolean interestedInBuying
) {}

