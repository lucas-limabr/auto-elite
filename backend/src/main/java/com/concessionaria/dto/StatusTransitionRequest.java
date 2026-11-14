package com.concessionaria.dto;

import com.concessionaria.enums.TestDriveStatus;
import jakarta.validation.constraints.NotNull;

public record StatusTransitionRequest(
        @NotNull(message = "Novo status é obrigatório")
        TestDriveStatus newStatus,

        String operatorNotes
) {}

