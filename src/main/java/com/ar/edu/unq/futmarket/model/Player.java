package com.ar.edu.unq.futmarket.model;

import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String team;

    @Column(nullable = false)
    private String league;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerPosition playerPosition;

    @Transient
    private int issuedTokens = 100;

    @Column(nullable = false)
    private int availableTokens = 100;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal currentTokenPrice = BigDecimal.ONE;

    private double goals;
    private double assists;
    private double shots;
    private double keyPasses;
    private double dribbles;
    private double tackles;
    private double interceptions;
    private double rating;

    @Column(unique = true)
    private Long externalId;

    @Version
    private Long version;

    public Player(String name, String team, String league, PlayerPosition playerPosition) {
        this.name = name;
        this.team = team;
        this.league = league;
        this.playerPosition = playerPosition;
    }

    public void subAvailableTokens(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("token quantity must be greater than zero");
        }
        if (quantity > this.availableTokens) {
            throw new IllegalArgumentException("cannot buy more tokens than available");
        }
        this.availableTokens -= quantity;
    }

    public void addAvailableTokens(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (this.availableTokens + quantity > this.issuedTokens) {
            throw new IllegalArgumentException("Cannot return more tokens than the total issued amount");
        }
        this.availableTokens += quantity;
    }
}

