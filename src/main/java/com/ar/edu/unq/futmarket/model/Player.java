package com.ar.edu.unq.futmarket.model;

import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private int issuedTokens = 100;

    @Builder.Default
    @Column(nullable = false)
    private int availableTokens = 100;

    @Column(nullable = false, precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal currentTokenPrice = BigDecimal.ONE;

    @Builder.Default
    private double goals = 0;

    @Builder.Default
    private double assists = 0;

    @Builder.Default
    private double shots = 0;

    @Builder.Default
    private double keyPasses = 0;

    @Builder.Default
    private double dribbles = 0;

    @Builder.Default
    private double tackles = 0;

    @Builder.Default
    private double interceptions = 0;

    @Builder.Default
    private double rating = 0.0;

    @Column(unique = true)
    private Long externalId;

    @Version
    private Long version;

    @Column(name = "last_stats_sync")
    private LocalDateTime lastStatsSync;

    public void addAvailableTokens(int amount) {
        this.availableTokens += amount;
    }

    public void subAvailableTokens(int amount) {
        if (amount > this.availableTokens) {
            throw new IllegalArgumentException("Not enough available tokens");
        }
        this.availableTokens -= amount;
    }
}

