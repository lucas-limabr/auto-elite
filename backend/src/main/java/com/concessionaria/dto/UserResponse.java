package com.concessionaria.dto;

import com.concessionaria.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Boolean active,
        LocalDateTime createdAt
) {}

