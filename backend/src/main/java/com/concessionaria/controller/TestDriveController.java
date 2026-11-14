package com.concessionaria.controller;

import com.concessionaria.dto.*;
import com.concessionaria.entity.User;
import com.concessionaria.enums.Role;
import com.concessionaria.enums.TestDriveStatus;
import com.concessionaria.service.TestDriveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-drives")
@RequiredArgsConstructor
public class TestDriveController {

    private final TestDriveService testDriveService;

    @PostMapping("/schedule")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TestDriveResponse> scheduleTestDrive(
            @Valid @RequestBody ScheduleTestDriveRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(testDriveService.validateAndScheduleTestDrive(user.getId(), request));
    }

    @GetMapping("/my-appointments")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<TestDriveResponse>> getMyAppointments(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(testDriveService.getAppointmentsByCustomer(user.getId()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TestDriveResponse> cancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(testDriveService.cancelAppointmentByCustomer(user.getId(), id));
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(testDriveService.submitFeedback(user.getId(), id, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<List<TestDriveResponse>> getAllAppointments(
            @RequestParam(required = false) TestDriveStatus status
    ) {
        return ResponseEntity.ok(testDriveService.getAllAppointments(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<TestDriveResponse> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(testDriveService.getAppointmentById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<TestDriveResponse> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusTransitionRequest request,
            @AuthenticationPrincipal User user
    ) {
        Role role = user != null ? user.getRole() : Role.OPERATOR;
        String email = user != null ? user.getEmail() : "SYSTEM";
        return ResponseEntity.ok(testDriveService.processAppointmentStatusTransition(id, request, email, role));
    }
}

