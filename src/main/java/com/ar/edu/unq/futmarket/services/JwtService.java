package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.controllers.request.LoginRequest;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    public String generateToken(String username);

    public String extractUsername(String token);

    public boolean isTokenValid(String token, UserDetails userDetails);
}
