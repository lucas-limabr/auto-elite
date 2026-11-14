package com.concessionaria.dto;

import com.concessionaria.enums.TestDriveStatus;

import java.time.LocalDateTime;

public record TestDriveResponse(
        Long id,
        LocalDateTime scheduledDateTime,
        TestDriveStatus status,
        String customerNotes,
        String operatorNotes,
        CustomerResponse customer,
        VehicleResponse vehicle,
        String approvedByOperatorName,
        FeedbackResponse feedback
) {}

