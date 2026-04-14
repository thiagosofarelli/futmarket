package com.ar.edu.unq.futmarket.controllers.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
}
