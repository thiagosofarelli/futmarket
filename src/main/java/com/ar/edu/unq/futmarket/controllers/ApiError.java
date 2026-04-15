package com.ar.edu.unq.futmarket.controllers;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiError {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ApiError(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ApiError(int status, String error, String message) {
        this(status, error, message, null);
    }

}
