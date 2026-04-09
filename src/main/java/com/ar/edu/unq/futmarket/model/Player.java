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

    @Column(nullable = false, updatable = false)
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
}

