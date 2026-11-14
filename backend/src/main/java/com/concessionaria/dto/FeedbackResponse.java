package com.concessionaria.dto;

public record FeedbackResponse(
        Long id,
        Integer rating,
        String comments,
        Boolean interestedInBuying,
        Long appointmentId
) {}

