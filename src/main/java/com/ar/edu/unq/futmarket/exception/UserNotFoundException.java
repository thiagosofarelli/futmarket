package com.ar.edu.unq.futmarket.exception;

public class UserNotFoundException extends Exception {
    @Override
    public String getMessage() {
        return "User not found.";
    }
}
