package com.ar.edu.unq.futmarket.model.enums;

import com.ar.edu.unq.futmarket.exception.LeagueNotFoundException;
import lombok.Getter;

@Getter
public enum League {
    PL("Premier League"),
    PD("La Liga"),
    FL1("Ligue 1"),
    BL1("Bundesliga"),
    SA("Serie A");

    private final String fullName;

    League(String fullName) {
        this.fullName = fullName;
    }

    public static String getFullNameByCode(String code) {
        try {
            return League.valueOf(code.toUpperCase()).getFullName();
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new LeagueNotFoundException();
        }
    }
}