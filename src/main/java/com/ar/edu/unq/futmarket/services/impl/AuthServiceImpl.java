package com.ar.edu.unq.futmarket.services.impl;

import com.ar.edu.unq.futmarket.controllers.request.LoginRequest;
import com.ar.edu.unq.futmarket.controllers.request.RegisterRequest;
import com.ar.edu.unq.futmarket.controllers.response.AuthResponse;
import com.ar.edu.unq.futmarket.exception.InvalidCredentialsException;
import com.ar.edu.unq.futmarket.exception.UsernameAlreadyExistsException;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
import com.ar.edu.unq.futmarket.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements UserDetailsService, AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServiceImpl jwtService;
    private final UserServiceImpl userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword() != null ? user.getPassword() : "")
            .roles(user.isSuperuser() ? "SUPERUSER" : "USER")
            .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException();
        }
        User user = new User(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.saveAndFlush(user);
        return new AuthResponse(jwtService.generateToken(request.getUsername()));
    }

    public AuthResponse login(LoginRequest request) {
        UserDetails userDetails = loadUserByUsername(request.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return new AuthResponse(jwtService.generateToken(request.getUsername()));
    }
}
