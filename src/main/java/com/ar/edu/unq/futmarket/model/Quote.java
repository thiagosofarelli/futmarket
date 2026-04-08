package com.ar.edu.unq.futmarket.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quotes")
@Getter
@Setter
@NoArgsConstructor
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "token_value", nullable = false, precision = 10, scale = 4)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValuationStrategy strategy;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    @PrePersist
    private void prePersist() {
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
    }
}

