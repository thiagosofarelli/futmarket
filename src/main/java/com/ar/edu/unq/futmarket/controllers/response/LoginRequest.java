package com.ar.edu.unq.futmarket.controllers.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}
