package com.concessionaria.controller;

import com.concessionaria.dto.AuditLogResponse;
import com.concessionaria.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }

    @GetMapping("/entity/{entityName}")
    public ResponseEntity<List<AuditLogResponse>> getLogsByEntity(@PathVariable String entityName) {
        return ResponseEntity.ok(auditService.getLogsByEntity(entityName));
    }
}

