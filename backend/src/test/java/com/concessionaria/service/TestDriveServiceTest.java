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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestDriveServiceTest {

    @Mock
    private TestDriveAppointmentRepository appointmentRepository;

    @Mock
    private TestDriveFeedbackRepository feedbackRepository;

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @Mock
    private OperatorProfileRepository operatorProfileRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TestDriveService testDriveService;

    private User customerUser;
    private CustomerProfile customerProfile;
    private DealershipBranch branch;
    private Brand brand;
    private Category suvCategory;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        customerUser = User.builder()
                .id(1L)
                .name("Carlos Silva")
                .email("carlos@gmail.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        customerProfile = CustomerProfile.builder()
                .id(1L)
                .user(customerUser)
                .cpf("123.456.789-00")
                .phone("(11) 98888-7777")
                .cnhNumber("12345678900")
                .birthDate(LocalDate.now().minusYears(30))
                .cnhExpirationDate(LocalDate.now().plusYears(2))
                .build();

        branch = DealershipBranch.builder()
                .id(1L)
                .name("Matriz SP")
                .cnpj("11.222.333/0001-44")
                .city("São Paulo")
                .state("SP")
                .address("Av. Paulista, 1000")
                .maxDailyTestDrives(10)
                .build();

        brand = Brand.builder().id(1L).name("BMW").active(true).build();
        suvCategory = Category.builder().id(1L).name("SUV").build();

        vehicle = Vehicle.builder()
                .id(1L)
                .model("BMW X5")
                .yearManufacture(2023)
                .yearModel(2024)
                .price(new BigDecimal("500000.00"))
                .mileage(10000)
                .color("Preto")
                .status(VehicleStatus.AVAILABLE)
                .brand(brand)
                .category(suvCategory)
                .branch(branch)
                .build();
    }

    private LocalDateTime getNextValidCommercialDateTime() {
        LocalDateTime dt = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        if (dt.getDayOfWeek().getValue() == 7) { // Domingo
            dt = dt.plusDays(1);
        }
        return dt;
    }

    @Test
    @DisplayName("Deve agendar test-drive com sucesso quando todos os dados forem válidos")
    void validateAndScheduleTestDrive_Success() {
        LocalDateTime validDate = getNextValidCommercialDateTime();
        ScheduleTestDriveRequest request = new ScheduleTestDriveRequest(1L, validDate, "Sem observações");

        when(customerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(customerProfile));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(appointmentRepository.findConflictingAppointments(eq(1L), any(), any())).thenReturn(List.of());
        when(appointmentRepository.countAppointmentsForBranchOnDate(eq(1L), any(), any())).thenReturn(2L);
        when(appointmentRepository.countCustomerRecentCancellations(eq(1L), any())).thenReturn(0L);

        TestDriveAppointment saved = TestDriveAppointment.builder()
                .id(10L)
                .customer(customerProfile)
                .vehicle(vehicle)
                .scheduledDateTime(validDate)
                .status(TestDriveStatus.PENDING)
                .build();
        when(appointmentRepository.save(any(TestDriveAppointment.class))).thenReturn(saved);

        TestDriveResponse response = testDriveService.validateAndScheduleTestDrive(1L, request);

        assertNotNull(response);
        assertEquals(TestDriveStatus.PENDING, response.status());
        verify(appointmentRepository).save(any(TestDriveAppointment.class));
        verify(auditService).logAction(eq("SCHEDULE_TEST_DRIVE"), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o cliente não for encontrado")
    void validateAndScheduleTestDrive_ThrowsWhenCustomerNotFound() {
        ScheduleTestDriveRequest request = new ScheduleTestDriveRequest(1L, LocalDateTime.now().plusDays(2), null);
        when(customerProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                testDriveService.validateAndScheduleTestDrive(99L, request));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o veículo não for encontrado")
    void validateAndScheduleTestDrive_ThrowsWhenVehicleNotFound() {
        ScheduleTestDriveRequest request = new ScheduleTestDriveRequest(99L, LocalDateTime.now().plusDays(2), null);
        when(customerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(customerProfile));
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                testDriveService.validateAndScheduleTestDrive(1L, request));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o agendamento for realizado com menos de 2 horas de antecedência")
    void validateAndScheduleTestDrive_ThrowsWhenScheduledTooSoon() {
        LocalDateTime tooSoon = LocalDateTime.now().plusMinutes(30);
        ScheduleTestDriveRequest request = new ScheduleTestDriveRequest(1L, tooSoon, null);

        when(customerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(customerProfile));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                testDriveService.validateAndScheduleTestDrive(1L, request));
        assertTrue(ex.getMessage().contains("antecedência mínima de 2 horas"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o agendamento for para mais de 30 dias no futuro")
    void validateAndScheduleTestDrive_ThrowsWhenScheduledTooFarInFuture() {
        LocalDateTime farFuture = LocalDateTime.now().plusDays(35);
        ScheduleTestDriveRequest request = new ScheduleTestDriveRequest(1L, farFuture, null);

        when(customerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(customerProfile));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                testDriveService.validateAndScheduleTestDrive(1L, request));
        assertTrue(ex.getMessage().contains("30 dias"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando agendado para um domingo")
    void validateAndScheduleTestDrive_ThrowsWhenScheduledOnSunday() {
        LocalDateTime nextDate = LocalDateTime.now().plusDays(2).withHour(10);
        while (nextDate.getDayOfWeek().getValue() != 7) {
            nextDate = nextDate.plusDays(1);
        }
        ScheduleTestDriveRequest request = new ScheduleTestDriveRequest(1L, nextDate, null);

        when(customerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(customerProfile));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                testDriveService.validateAndScheduleTestDrive(1L, request));
        assertTrue(ex.getMessage().contains("domingos"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando agendado fora do horário comercial (ex: 20:00)")
    void validateAndScheduleTestDrive_ThrowsWhenScheduledOutsideBusinessHours() {
        LocalDateTime nightTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0);
        if (nightTime.getDayOfWeek().getValue() == 7) nightTime = nightTime.plusDays(1);
        ScheduleTestDriveRequest request = new ScheduleTestDriveRequest(1L, nightTime, null);

        when(customerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(customerProfile));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                testDriveService.validateAndScheduleTestDrive(1L, request));
        assertTrue(ex.getMessage().contains("horário comercial"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando a CNH do condutor estiver vencida na data marcada")
    void validateAndScheduleTestDrive_ThrowsWhenCnhExpired() {
        customerProfile.setCnhExpirationDate(LocalDate.now().minusDays(1));
        LocalDateTime validDate = getNextValidCommercialDateTime();
        ScheduleTestDriveRequest request = new ScheduleTestDriveRequest(1L, validDate, null);

        when(customerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(customerProfile));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                testDriveService.validateAndScheduleTestDrive(1L, request));
        assertTrue(ex.getMessage().contains("vencida"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando cliente tiver menos de 25 anos para SUV/Luxo/Elétrico")
    void validateAndScheduleTestDrive_ThrowsWhenUnderageForLuxuryOrSuv() {
        customerProfile.setBirthDate(LocalDate.now().minusYears(22)); // 22 anos
        LocalDateTime validDate = getNextValidCommercialDateTime();
        ScheduleTestDriveRequest request = new ScheduleTestDriveRequest(1L, validDate, null);

        when(customerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(customerProfile));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                testDriveService.validateAndScheduleTestDrive(1L, request));
        assertTrue(ex.getMessage().contains("25 anos"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o veículo não estiver disponível (ex: IN_TEST_DRIVE)")
    void validateAndScheduleTestDrive_ThrowsWhenVehicleNotAvailable() {
        vehicle.setStatus(VehicleStatus.IN_TEST_DRIVE);
        LocalDateTime validDate = getNextValidCommercialDateTime();
        ScheduleTestDriveRequest request = new ScheduleTestDriveRequest(1L, validDate, null);

        when(customerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(customerProfile));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                testDriveService.validateAndScheduleTestDrive(1L, request));
        assertTrue(ex.getMessage().contains("não está disponível"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando houver conflito de horário para o mesmo veículo")
    void validateAndScheduleTestDrive_ThrowsWhenScheduleConflict() {
        LocalDateTime validDate = getNextValidCommercialDateTime();
        ScheduleTestDriveRequest request = new ScheduleTestDriveRequest(1L, validDate, null);

        when(customerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(customerProfile));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        TestDriveAppointment conflict = TestDriveAppointment.builder().id(50L).build();
        when(appointmentRepository.findConflictingAppointments(eq(1L), any(), any())).thenReturn(List.of(conflict));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                testDriveService.validateAndScheduleTestDrive(1L, request));
        assertTrue(ex.getMessage().contains("Já existe outro test-drive agendado"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando a filial atingir seu limite diário de test-drives")
    void validateAndScheduleTestDrive_ThrowsWhenDailyBranchLimitReached() {
        LocalDateTime validDate = getNextValidCommercialDateTime();
        ScheduleTestDriveRequest request = new ScheduleTestDriveRequest(1L, validDate, null);

        when(customerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(customerProfile));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(appointmentRepository.findConflictingAppointments(eq(1L), any(), any())).thenReturn(List.of());
        when(appointmentRepository.countAppointmentsForBranchOnDate(eq(1L), any(), any())).thenReturn(10L); // max is 10

        BusinessException ex = assertThrows(BusinessException.class, () ->
                testDriveService.validateAndScheduleTestDrive(1L, request));
        assertTrue(ex.getMessage().contains("Limite diário"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o cliente tiver 3 ou mais cancelamentos recentes")
    void validateAndScheduleTestDrive_ThrowsWhenTooManyRecentCancellations() {
        LocalDateTime validDate = getNextValidCommercialDateTime();
        ScheduleTestDriveRequest request = new ScheduleTestDriveRequest(1L, validDate, null);

        when(customerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(customerProfile));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(appointmentRepository.findConflictingAppointments(eq(1L), any(), any())).thenReturn(List.of());
        when(appointmentRepository.countAppointmentsForBranchOnDate(eq(1L), any(), any())).thenReturn(2L);
        when(appointmentRepository.countCustomerRecentCancellations(eq(1L), any())).thenReturn(3L);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                testDriveService.validateAndScheduleTestDrive(1L, request));
        assertTrue(ex.getMessage().contains("bloqueado temporariamente"));
    }

    @Test
    @DisplayName("Operador deve aprovar agendamento PENDING com sucesso")
    void processAppointmentStatusTransition_Approve_Success() {
        TestDriveAppointment appointment = TestDriveAppointment.builder()
                .id(1L)
                .status(TestDriveStatus.PENDING)
                .customer(customerProfile)
                .vehicle(vehicle)
                .scheduledDateTime(LocalDateTime.now().plusDays(2))
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(operatorProfileRepository.findByUserEmail("operador@concessionaria.com")).thenReturn(Optional.empty());
        when(appointmentRepository.save(any(TestDriveAppointment.class))).thenAnswer(i -> i.getArgument(0));

        StatusTransitionRequest req = new StatusTransitionRequest(TestDriveStatus.APPROVED, "Aprovado pelo operador");
        TestDriveResponse res = testDriveService.processAppointmentStatusTransition(1L, req, "operador@concessionaria.com", Role.OPERATOR);

        assertNotNull(res);
        assertEquals(TestDriveStatus.APPROVED, res.status());
    }

    @Test
    @DisplayName("Deve concluir agendamento APPROVED -> COMPLETED e restaurar veículo para AVAILABLE")
    void processAppointmentStatusTransition_Complete_Success() {
        vehicle.setStatus(VehicleStatus.IN_TEST_DRIVE);
        TestDriveAppointment appointment = TestDriveAppointment.builder()
                .id(1L)
                .status(TestDriveStatus.APPROVED)
                .customer(customerProfile)
                .vehicle(vehicle)
                .scheduledDateTime(LocalDateTime.now().minusHours(1))
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(operatorProfileRepository.findByUserEmail(anyString())).thenReturn(Optional.empty());
        when(appointmentRepository.save(any(TestDriveAppointment.class))).thenAnswer(i -> i.getArgument(0));

        StatusTransitionRequest req = new StatusTransitionRequest(TestDriveStatus.COMPLETED, "Test drive realizado");
        TestDriveResponse res = testDriveService.processAppointmentStatusTransition(1L, req, "operador@concessionaria.com", Role.OPERATOR);

        assertNotNull(res);
        assertEquals(TestDriveStatus.COMPLETED, res.status());
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar alterar status de agendamento já COMPLETED")
    void processAppointmentStatusTransition_ThrowsWhenTransitionFromCompleted() {
        TestDriveAppointment appointment = TestDriveAppointment.builder()
                .id(1L)
                .status(TestDriveStatus.COMPLETED)
                .customer(customerProfile)
                .vehicle(vehicle)
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        StatusTransitionRequest req = new StatusTransitionRequest(TestDriveStatus.CANCELED, null);
        assertThrows(BusinessException.class, () ->
                testDriveService.processAppointmentStatusTransition(1L, req, "operador@concessionaria.com", Role.OPERATOR));
    }

    @Test
    @DisplayName("Cliente deve cancelar agendamento com sucesso com antecedência mínima de 4 horas")
    void processAppointmentStatusTransition_CustomerCancels_Success() {
        TestDriveAppointment appointment = TestDriveAppointment.builder()
                .id(1L)
                .status(TestDriveStatus.PENDING)
                .customer(customerProfile)
                .vehicle(vehicle)
                .scheduledDateTime(LocalDateTime.now().plusHours(10))
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(TestDriveAppointment.class))).thenAnswer(i -> i.getArgument(0));

        StatusTransitionRequest req = new StatusTransitionRequest(TestDriveStatus.CANCELED, "Cliente desistiu");
        TestDriveResponse res = testDriveService.processAppointmentStatusTransition(1L, req, "carlos@gmail.com", Role.CUSTOMER);

        assertNotNull(res);
        assertEquals(TestDriveStatus.CANCELED, res.status());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando cliente tentar cancelar com menos de 4 horas de antecedência")
    void processAppointmentStatusTransition_ThrowsWhenCustomerCancelsWithoutAdvanceNotice() {
        TestDriveAppointment appointment = TestDriveAppointment.builder()
                .id(1L)
                .status(TestDriveStatus.PENDING)
                .customer(customerProfile)
                .vehicle(vehicle)
                .scheduledDateTime(LocalDateTime.now().plusHours(2)) // Apenas 2h
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        StatusTransitionRequest req = new StatusTransitionRequest(TestDriveStatus.CANCELED, "Cancelando em cima da hora");
        BusinessException ex = assertThrows(BusinessException.class, () ->
                testDriveService.processAppointmentStatusTransition(1L, req, "carlos@gmail.com", Role.CUSTOMER));
        assertTrue(ex.getMessage().contains("antecedência mínima de 4 horas"));
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando cliente tentar alterar status para APPROVED")
    void processAppointmentStatusTransition_ThrowsWhenCustomerTriesToApprove() {
        TestDriveAppointment appointment = TestDriveAppointment.builder()
                .id(1L)
                .status(TestDriveStatus.PENDING)
                .customer(customerProfile)
                .vehicle(vehicle)
                .scheduledDateTime(LocalDateTime.now().plusHours(10))
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        StatusTransitionRequest req = new StatusTransitionRequest(TestDriveStatus.APPROVED, null);
        assertThrows(UnauthorizedException.class, () ->
                testDriveService.processAppointmentStatusTransition(1L, req, "carlos@gmail.com", Role.CUSTOMER));
    }

    @Test
    @DisplayName("Deve submeter feedback com sucesso após agendamento COMPLETED")
    void submitFeedback_Success() {
        TestDriveAppointment appointment = TestDriveAppointment.builder()
                .id(1L)
                .status(TestDriveStatus.COMPLETED)
                .customer(customerProfile)
                .vehicle(vehicle)
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(feedbackRepository.existsByAppointmentId(1L)).thenReturn(false);

        TestDriveFeedback savedFb = TestDriveFeedback.builder()
                .id(100L)
                .rating(5)
                .comments("Excelente dirigibilidade")
                .interestedInBuying(true)
                .appointment(appointment)
                .build();
        when(feedbackRepository.save(any(TestDriveFeedback.class))).thenReturn(savedFb);

        FeedbackRequest req = new FeedbackRequest(5, "Excelente dirigibilidade", true);
        FeedbackResponse res = testDriveService.submitFeedback(1L, 1L, req);

        assertNotNull(res);
        assertEquals(5, res.rating());
        assertTrue(res.interestedInBuying());
        verify(feedbackRepository).save(any(TestDriveFeedback.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar submeter feedback em agendamento não COMPLETED")
    void submitFeedback_ThrowsWhenAppointmentNotCompleted() {
        TestDriveAppointment appointment = TestDriveAppointment.builder()
                .id(1L)
                .status(TestDriveStatus.APPROVED) // ainda APPROVED
                .customer(customerProfile)
                .vehicle(vehicle)
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        FeedbackRequest req = new FeedbackRequest(5, "Muito bom", true);
        assertThrows(BusinessException.class, () -> testDriveService.submitFeedback(1L, 1L, req));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar enviar feedback duplicado")
    void submitFeedback_ThrowsWhenAlreadySubmitted() {
        TestDriveAppointment appointment = TestDriveAppointment.builder()
                .id(1L)
                .status(TestDriveStatus.COMPLETED)
                .customer(customerProfile)
                .vehicle(vehicle)
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(feedbackRepository.existsByAppointmentId(1L)).thenReturn(true);

        FeedbackRequest req = new FeedbackRequest(4, "Bom", false);
        assertThrows(BusinessException.class, () -> testDriveService.submitFeedback(1L, 1L, req));
    }
}

