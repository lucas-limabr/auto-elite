package com.concessionaria.controller;

import com.concessionaria.dto.BrandRequest;
import com.concessionaria.dto.BrandResponse;
import com.concessionaria.entity.User;
import com.concessionaria.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<List<BrandResponse>> listBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<BrandResponse> createBrand(
            @Valid @RequestBody BrandRequest request,
            @AuthenticationPrincipal User user
    ) {
        String email = user != null ? user.getEmail() : "OPERATOR";
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.createBrand(request, email));
    }
}

