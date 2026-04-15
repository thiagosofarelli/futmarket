package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.controllers.response.AuthResponse;
import com.ar.edu.unq.futmarket.controllers.response.LoginRequest;
import com.ar.edu.unq.futmarket.controllers.response.RegisterRequest;
import com.ar.edu.unq.futmarket.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
