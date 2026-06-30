package com.ar.edu.unq.futmarket.exception;

public class InvalidTokenQuantityException extends RuntimeException {
    public InvalidTokenQuantityException() {
        super("Invalid token quantity.");
    }
}
