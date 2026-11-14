package com.concessionaria.service;

import com.concessionaria.dto.*;
import com.concessionaria.entity.*;
import com.concessionaria.enums.Role;
import com.concessionaria.enums.TestDriveStatus;
import com.concessionaria.enums.VehicleStatus;
import com.concessionaria.exception.BusinessException;
import com.concessionaria.exception.ResourceNotFoundException;
import com.concessionaria.exception.UnauthorizedException;
import com.concessionaria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestDriveService {

    private final TestDriveAppointmentRepository appointmentRepository;
    private final TestDriveFeedbackRepository feedbackRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final OperatorProfileRepository operatorProfileRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;
    private final AuditService auditService;

    /**
     * CODE SMELL INTENCIONAL: Complexidade Ciclomática (McCabe) &gt; 10 (Alvo: CC ~ 15).
     * Valida integralmente e agenda um test-drive de veículo, aplicando regras de CNH vencida,
     * restrição de idade por categoria, horário comercial de atendimento, conflito de agenda,
     * capacidade diária da filial e histórico de cancelamentos do cliente.
     * Toda a lógica reside em um único método com múltiplos branches propositais.
     */
    @Transactional
    public TestDriveResponse validateAndScheduleTestDrive(Long customerUserId, ScheduleTestDriveRequest request) {
        // 1. Busca perfil do cliente
        CustomerProfile customer = customerProfileRepository.findByUserId(customerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de cliente não encontrado para o usuário: " + customerUserId));

        // 2. Busca veículo e filial
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com ID: " + request.vehicleId()));

        LocalDateTime scheduled = request.scheduledDateTime();
        LocalDateTime now = LocalDateTime.now();

        // 3. Validação de Antecedência Mínima e Máxima (CC branch 1, 2)
        if (scheduled.isBefore(now.plusHours(2))) {
            throw new BusinessException("O agendamento deve ser realizado com antecedência mínima de 2 horas");
        }
        if (scheduled.isAfter(now.plusDays(30))) {
            throw new BusinessException("O agendamento não pode ultrapassar 30 dias de antecedência");
        }

        // 4. Validação de Dias e Horário Comercial (Segunda a Sábado, 08:00 às 18:00) (CC branch 3, 4, 5)
        DayOfWeek dayOfWeek = scheduled.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            throw new BusinessException("A concessionária não realiza test-drives aos domingos");
        }
        LocalTime time = scheduled.toLocalTime();
        if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(17, 30))) {
            throw new BusinessException("Agendamentos permitidos apenas em horário comercial (08:00 às 17:30)");
        }

        // 5. Validação de CNH Vencida (CC branch 6)
        if (customer.getCnhExpirationDate().isBefore(scheduled.toLocalDate())) {
            throw new BusinessException("A CNH do cliente estará vencida na data do test-drive: " + customer.getCnhExpirationDate());
        }

        // 6. Validação de Idade e Restrição por Categoria Especial (CC branch 7, 8, 9)
        int customerAge = Period.between(customer.getBirthDate(), LocalDate.now()).getYears();
        String catName = vehicle.getCategory().getName().toUpperCase();
        if ((catName.contains("LUXO") || catName.contains("ELÉTRICO") || catName.contains("ELETRICO") || catName.contains("SUV")) && customerAge < 25) {
            throw new BusinessException("Veículos de luxo, SUVs ou elétricos exigem que o condutor tenha no mínimo 25 anos de idade");
        } else if (customerAge < 18) {
            throw new BusinessException("Condutor deve ser maior de 18 anos");
        }

        // 7. Status do Veículo (CC branch 10)
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new BusinessException("O veículo não está disponível para test-drive no momento (Status atual: " + vehicle.getStatus() + ")");
        }

        // 8. Conflito de Horário para o mesmo veículo (Intervalo de 1 hora) (CC branch 11)
        LocalDateTime windowStart = scheduled.minusMinutes(59);
        LocalDateTime windowEnd = scheduled.plusMinutes(59);
        List<TestDriveAppointment> conflicts = appointmentRepository.findConflictingAppointments(vehicle.getId(), windowStart, windowEnd);
        if (!conflicts.isEmpty()) {
            throw new BusinessException("Já existe outro test-drive agendado para este veículo no horário selecionado");
        }

        // 9. Limite Diário de Test-Drives na Filial (CC branch 12)
        LocalDateTime startOfDay = scheduled.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = scheduled.toLocalDate().atTime(LocalTime.MAX);
        long dailyCount = appointmentRepository.countAppointmentsForBranchOnDate(vehicle.getBranch().getId(), startOfDay, endOfDay);
        if (dailyCount >= vehicle.getBranch().getMaxDailyTestDrives()) {
            throw new BusinessException("Limite diário de test-drives atingido para a filial " + vehicle.getBranch().getName() + " nesta data");
        }

        // 10. Bloqueio por Cancelamentos Recentes do Cliente (>= 3 nos últimos 30 dias) (CC branch 13)
        long recentCancellations = appointmentRepository.countCustomerRecentCancellations(customer.getId(), now.minusDays(30));
        if (recentCancellations >= 3) {
            throw new BusinessException("Cliente bloqueado temporariamente para novos agendamentos devido a 3 ou mais cancelamentos recentes");
        }

        TestDriveAppointment appointment = TestDriveAppointment.builder()
                .customer(customer)
                .vehicle(vehicle)
                .scheduledDateTime(scheduled)
                .status(TestDriveStatus.PENDING)
                .customerNotes(request.customerNotes())
                .build();

        TestDriveAppointment saved = appointmentRepository.save(appointment);
        auditService.logAction("SCHEDULE_TEST_DRIVE", "TestDriveAppointment", saved.getId().toString(), customer.getUser().getEmail(), "Agendamento solicitado para o veículo " + vehicle.getModel());

        return toDto(saved);
    }

    /**
     * CODE SMELL INTENCIONAL: Complexidade Ciclomática (McCabe) &gt; 10 (Alvo: CC ~ 14).
     * Máquina de estados complexa para transição de status do agendamento (PENDING, APPROVED, REJECTED, COMPLETED, CANCELED),
     * validando permissões de papéis (Role), prazos mínimos de cancelamento, atribuição de operador
     * e atualização em cascata do status do veículo associado.
     * Toda a lógica reside em um único método com múltiplos branches propositais.
     */
    @Transactional
    public TestDriveResponse processAppointmentStatusTransition(Long appointmentId, StatusTransitionRequest request, String performedByEmail, Role userRole) {
        TestDriveAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + appointmentId));

        TestDriveStatus current = appointment.getStatus();
        TestDriveStatus target = request.newStatus();
        LocalDateTime now = LocalDateTime.now();

        // 1. Estados finais imutáveis (CC branch 1, 2, 3)
        if (current == TestDriveStatus.COMPLETED) {
            throw new BusinessException("Agendamento já concluído não pode ter seu status alterado");
        }
        if (current == TestDriveStatus.REJECTED) {
            throw new BusinessException("Agendamento já rejeitado não pode ser reaberto");
        }
        if (current == TestDriveStatus.CANCELED) {
            throw new BusinessException("Agendamento cancelado não pode ter status alterado");
        }

        // 2. Validação por Papel do Usuário (CC branch 4, 5)
        if (userRole == Role.CUSTOMER) {
            if (target != TestDriveStatus.CANCELED) {
                throw new UnauthorizedException("Clientes possuem permissão apenas para cancelar seus próprios agendamentos");
            }
            if (!appointment.getCustomer().getUser().getEmail().equalsIgnoreCase(performedByEmail)) {
                throw new UnauthorizedException("Você não tem permissão para cancelar o agendamento de outro cliente");
            }
            // Antecedência mínima para cancelamento pelo cliente (4 horas)
            if (appointment.getScheduledDateTime().isBefore(now.plusHours(4))) {
                throw new BusinessException("Cancelamentos pelo cliente exigem antecedência mínima de 4 horas antes do horário marcado");
            }
        }

        // 3. Regras de Transição de Estado (CC branch 6, 7, 8, 9, 10)
        OperatorProfile operator = null;
        if (userRole == Role.OPERATOR || userRole == Role.ADMIN) {
            operator = operatorProfileRepository.findByUserEmail(performedByEmail).orElse(null);
        }

        if (current == TestDriveStatus.PENDING) {
            if (target == TestDriveStatus.APPROVED) {
                appointment.setStatus(TestDriveStatus.APPROVED);
                if (operator != null) {
                    appointment.setApprovedBy(operator);
                }
            } else if (target == TestDriveStatus.REJECTED) {
                appointment.setStatus(TestDriveStatus.REJECTED);
            } else if (target == TestDriveStatus.CANCELED) {
                appointment.setStatus(TestDriveStatus.CANCELED);
            } else {
                throw new BusinessException("Transição inválida de PENDING para " + target);
            }
        } else if (current == TestDriveStatus.APPROVED) {
            if (target == TestDriveStatus.COMPLETED) {
                appointment.setStatus(TestDriveStatus.COMPLETED);
                // Retorna veículo para status AVAILABLE
                Vehicle vehicle = appointment.getVehicle();
                if (vehicle.getStatus() == VehicleStatus.IN_TEST_DRIVE) {
                    vehicle.setStatus(VehicleStatus.AVAILABLE);
                    vehicleRepository.save(vehicle);
                }
            } else if (target == TestDriveStatus.CANCELED) {
                appointment.setStatus(TestDriveStatus.CANCELED);
                Vehicle vehicle = appointment.getVehicle();
                if (vehicle.getStatus() == VehicleStatus.IN_TEST_DRIVE) {
                    vehicle.setStatus(VehicleStatus.AVAILABLE);
                    vehicleRepository.save(vehicle);
                }
            } else {
                throw new BusinessException("Transição inválida de APPROVED para " + target);
            }
        }

        // 4. Notas do operador (CC branch 11)
        if (request.operatorNotes() != null && !request.operatorNotes().isBlank()) {
            appointment.setOperatorNotes(request.operatorNotes());
        }

        TestDriveAppointment updated = appointmentRepository.save(appointment);
        auditService.logAction("TRANSITION_APPOINTMENT_STATUS", "TestDriveAppointment", updated.getId().toString(), performedByEmail, "Status transicionado de " + current + " para " + target);

        return toDto(updated);
    }

    /**
     * Retorna a lista de agendamentos de um cliente específico.
     * Complexidade Ciclomática (McCabe): 2 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public List<TestDriveResponse> getAppointmentsByCustomer(Long customerUserId) {
        CustomerProfile customer = customerProfileRepository.findByUserId(customerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de cliente não encontrado"));

        return appointmentRepository.findByCustomerId(customer.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Cancela um agendamento a pedido do cliente.
     * Complexidade Ciclomática (McCabe): 3 (CC &lt; 10 - Clean Code)
     */
    @Transactional
    public TestDriveResponse cancelAppointmentByCustomer(Long customerUserId, Long appointmentId) {
        CustomerProfile customer = customerProfileRepository.findByUserId(customerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de cliente não encontrado"));

        StatusTransitionRequest transitionRequest = new StatusTransitionRequest(TestDriveStatus.CANCELED, "Cancelado pelo cliente");
        return processAppointmentStatusTransition(appointmentId, transitionRequest, customer.getUser().getEmail(), Role.CUSTOMER);
    }

    /**
     * Submete feedback pós-test-drive para um agendamento já concluído.
     * Complexidade Ciclomática (McCabe): 3 (CC &lt; 10 - Clean Code)
     */
    @Transactional
    public FeedbackResponse submitFeedback(Long customerUserId, Long appointmentId, FeedbackRequest request) {
        TestDriveAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + appointmentId));

        if (!appointment.getCustomer().getUser().getId().equals(customerUserId)) {
            throw new UnauthorizedException("Você não tem permissão para enviar feedback neste agendamento");
        }

        if (appointment.getStatus() != TestDriveStatus.COMPLETED) {
            throw new BusinessException("Feedback só pode ser submetido após a conclusão do test-drive (Status atual: " + appointment.getStatus() + ")");
        }

        if (feedbackRepository.existsByAppointmentId(appointmentId)) {
            throw new BusinessException("Já existe uma avaliação registrada para este agendamento");
        }

        TestDriveFeedback feedback = TestDriveFeedback.builder()
                .appointment(appointment)
                .rating(request.rating())
                .comments(request.comments())
                .interestedInBuying(request.interestedInBuying())
                .build();

        TestDriveFeedback saved = feedbackRepository.save(feedback);
        auditService.logAction("SUBMIT_FEEDBACK", "TestDriveFeedback", saved.getId().toString(), appointment.getCustomer().getUser().getEmail(), "Feedback registrado com nota " + saved.getRating());

        return new FeedbackResponse(
                saved.getId(),
                saved.getRating(),
                saved.getComments(),
                saved.getInterestedInBuying(),
                appointment.getId()
        );
    }

    /**
     * Lista todos os agendamentos cadastrados, opcionalmente filtrados por status.
     * Complexidade Ciclomática (McCabe): 2 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public List<TestDriveResponse> getAllAppointments(TestDriveStatus status) {
        List<TestDriveAppointment> list = (status != null)
                ? appointmentRepository.findByStatus(status)
                : appointmentRepository.findAll();

        return list.stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Busca um agendamento por ID.
     * Complexidade Ciclomática (McCabe): 1 (CC &lt; 10 - Clean Code)
     */
    @Transactional(readOnly = true)
    public TestDriveResponse getAppointmentById(Long id) {
        TestDriveAppointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));
        return toDto(appointment);
    }

    public TestDriveResponse toDto(TestDriveAppointment a) {
        CustomerResponse customerDto = new CustomerResponse(
                a.getCustomer().getId(),
                a.getCustomer().getUser().getId(),
                a.getCustomer().getUser().getName(),
                a.getCustomer().getUser().getEmail(),
                a.getCustomer().getCpf(),
                a.getCustomer().getPhone(),
                a.getCustomer().getCnhNumber(),
                a.getCustomer().getCnhExpirationDate(),
                a.getCustomer().getBirthDate()
        );

        VehicleResponse vehicleDto = vehicleService.toDto(a.getVehicle());

        FeedbackResponse feedbackDto = null;
        if (a.getFeedback() != null) {
            feedbackDto = new FeedbackResponse(
                    a.getFeedback().getId(),
                    a.getFeedback().getRating(),
                    a.getFeedback().getComments(),
                    a.getFeedback().getInterestedInBuying(),
                    a.getId()
            );
        }

        String operatorName = a.getApprovedBy() != null ? a.getApprovedBy().getUser().getName() : null;

        return new TestDriveResponse(
                a.getId(),
                a.getScheduledDateTime(),
                a.getStatus(),
                a.getCustomerNotes(),
                a.getOperatorNotes(),
                customerDto,
                vehicleDto,
                operatorName,
                feedbackDto
        );
    }
}

