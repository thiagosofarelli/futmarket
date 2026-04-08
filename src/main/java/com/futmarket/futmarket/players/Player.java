package com.futmarket.futmarket.players;

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
    private Position position;

    /** Total tokens issued — always 100 per domain rules. */
    @Column(nullable = false)
    private int totalTokens = 100;

    /** Current market value per token in credits. */
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal currentValue = BigDecimal.ONE;

    // --- raw performance stats (cached from external APIs) ---

    private double goals;
    private double assists;
    private double shots;
    private double keyPasses;
    private double dribbles;
    private double tackles;
    private double interceptions;
    private double rating;

    /** External ID used by Football-Data.org */
    @Column(unique = true)
    private Long externalId;
}
