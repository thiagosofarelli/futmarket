package com.ar.edu.unq.futmarket.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException() {
        super("Username already exists.");
    }
}
