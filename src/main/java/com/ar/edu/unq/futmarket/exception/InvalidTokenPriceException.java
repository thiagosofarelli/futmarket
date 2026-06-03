package com.ar.edu.unq.futmarket.exception;

public class InvalidTokenPriceException extends RuntimeException {
    public InvalidTokenPriceException() {
        super("Invalid token price.");
    }
}
