package com.ar.edu.unq.futmarket.model;


import com.ar.edu.unq.futmarket.model.enums.ValuationStrategy;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quotes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal currentTokenPrice;

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

