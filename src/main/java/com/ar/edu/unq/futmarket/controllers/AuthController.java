package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.controllers.dto.UserDTO;
import com.ar.edu.unq.futmarket.controllers.response.AuthResponse;
import com.ar.edu.unq.futmarket.controllers.request.LoginRequest;
import com.ar.edu.unq.futmarket.controllers.request.RegisterRequest;
import com.ar.edu.unq.futmarket.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ModelMapper modelMapper;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequest request) {
        UserDTO dto = modelMapper.map(authService.register(request), UserDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequest request) {
        UserDTO dto = modelMapper.map(authService.login(request), UserDTO.class);
        return ResponseEntity.ok(dto);
    }
}
