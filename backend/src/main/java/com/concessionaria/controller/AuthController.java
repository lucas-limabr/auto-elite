package com.concessionaria.controller;

import com.concessionaria.config.JwtService;
import com.concessionaria.dto.AuthResponse;
import com.concessionaria.dto.CustomerResponse;
import com.concessionaria.dto.LoginRequest;
import com.concessionaria.dto.RegisterCustomerRequest;
import com.concessionaria.dto.UserResponse;
import com.concessionaria.entity.User;
import com.concessionaria.exception.ResourceNotFoundException;
import com.concessionaria.repository.UserRepository;
import com.concessionaria.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = (User) auth.getPrincipal();
        String token = jwtService.generateToken(user);
        UserResponse userResponse = userService.toUserDto(user);

        return ResponseEntity.ok(new AuthResponse(token, userResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterCustomerRequest request) {
        CustomerResponse customer = userService.registerCustomer(request);
        User user = userRepository.findById(customer.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário recém-criado não encontrado"));

        String token = jwtService.generateToken(user);
        UserResponse userResponse = userService.toUserDto(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, userResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
        return ResponseEntity.ok(userService.toUserDto(user));
    }
}

