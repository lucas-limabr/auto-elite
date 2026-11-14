package com.concessionaria.controller;

import com.concessionaria.dto.BranchRequest;
import com.concessionaria.dto.BranchResponse;
import com.concessionaria.entity.User;
import com.concessionaria.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public ResponseEntity<List<BranchResponse>> listBranches() {
        return ResponseEntity.ok(branchService.getAllBranches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchResponse> getBranchById(@PathVariable Long id) {
        return ResponseEntity.ok(branchService.getBranchById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchResponse> createBranch(
            @Valid @RequestBody BranchRequest request,
            @AuthenticationPrincipal User user
    ) {
        String email = user != null ? user.getEmail() : "ADMIN";
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.createBranch(request, email));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchResponse> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchRequest request,
            @AuthenticationPrincipal User user
    ) {
        String email = user != null ? user.getEmail() : "ADMIN";
        return ResponseEntity.ok(branchService.updateBranch(id, request, email));
    }
}

