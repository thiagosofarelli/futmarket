package com.ar.edu.unq.futmarket.exception;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException() {
        super("Player not found.");
    }
}
