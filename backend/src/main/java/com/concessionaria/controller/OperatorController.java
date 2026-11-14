package com.concessionaria.controller;

import com.concessionaria.dto.OperatorRequest;
import com.concessionaria.dto.OperatorResponse;
import com.concessionaria.entity.User;
import com.concessionaria.service.OperatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operators")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OperatorController {

    private final OperatorService operatorService;

    @GetMapping
    public ResponseEntity<List<OperatorResponse>> listOperators() {
        return ResponseEntity.ok(operatorService.getAllOperators());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperatorResponse> getOperatorById(@PathVariable Long id) {
        return ResponseEntity.ok(operatorService.getOperatorById(id));
    }

    @PostMapping
    public ResponseEntity<OperatorResponse> createOperator(
            @Valid @RequestBody OperatorRequest request,
            @AuthenticationPrincipal User user
    ) {
        String email = user != null ? user.getEmail() : "ADMIN";
        return ResponseEntity.status(HttpStatus.CREATED).body(operatorService.createOperator(request, email));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<OperatorResponse> toggleOperatorStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        String email = user != null ? user.getEmail() : "ADMIN";
        return ResponseEntity.ok(operatorService.toggleOperatorStatus(id, email));
    }
}

