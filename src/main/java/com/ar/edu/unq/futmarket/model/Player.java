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

    @Column(nullable = false)
    @Builder.Default
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

