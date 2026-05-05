package com.ar.edu.unq.futmarket.exception;

public class LeagueNotFoundException extends RuntimeException {
    public LeagueNotFoundException() {
        super("League not found.");
    }
}
