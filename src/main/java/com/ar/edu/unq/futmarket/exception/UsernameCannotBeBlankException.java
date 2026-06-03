package com.ar.edu.unq.futmarket.exception;

public class UsernameCannotBeBlankException extends RuntimeException {
    public UsernameCannotBeBlankException() {
        super("Username cannot be blank.");
    }
}
