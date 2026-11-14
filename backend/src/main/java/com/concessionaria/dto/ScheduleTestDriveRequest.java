package com.concessionaria.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ScheduleTestDriveRequest(
        @NotNull(message = "ID do veículo é obrigatório")
        Long vehicleId,

        @NotNull(message = "Data e hora do agendamento são obrigatórios")
        @Future(message = "O agendamento deve ser para uma data e hora futura")
        LocalDateTime scheduledDateTime,

        String customerNotes
) {}

