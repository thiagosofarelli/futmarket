package com.ar.edu.unq.futmarket.services.impl;

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
    public String register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException();
        }
        User user = new User(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.saveAndFlush(user);
        return jwtService.generateToken(username);
    }

    public String login(String username, String password) {
        UserDetails userDetails = loadUserByUsername(username);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return jwtService.generateToken(username);
    }
}
