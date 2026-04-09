package com.ar.edu.unq.futmarket.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(
        name = "positions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "player_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private int tokensAcquired;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal averagePurchasePrice = BigDecimal.ZERO;

    @Version
    private Long version;

    public void registerPurchase(int quantity, BigDecimal purchasePricePerToken) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("token quantity must be greater than zero");
        }
        if (purchasePricePerToken == null) {
            throw new IllegalArgumentException("purchase price must not be null");
        }

        BigDecimal currentCost = averagePurchasePrice.multiply(BigDecimal.valueOf(tokensAcquired));
        BigDecimal purchaseCost = purchasePricePerToken.multiply(BigDecimal.valueOf(quantity));
        tokensAcquired += quantity;
        averagePurchasePrice = currentCost.add(purchaseCost)
                .divide(BigDecimal.valueOf(tokensAcquired), 4, RoundingMode.HALF_UP);
    }

    public void registerSale(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("token quantity must be greater than zero");
        }
        if (quantity > tokensAcquired) {
            throw new IllegalArgumentException("cannot sell more tokens than currently held");
        }

        tokensAcquired -= quantity;
        if (tokensAcquired == 0) {
            averagePurchasePrice = BigDecimal.ZERO;
        }
    }

    @Transient
    public BigDecimal getCurrentValue() {
        if (player == null || player.getCurrentTokenPrice() == null) {
            return BigDecimal.ZERO;
        }
        return player.getCurrentTokenPrice().multiply(BigDecimal.valueOf(tokensAcquired));
    }

    @Transient
    public BigDecimal getProfitLoss() {
        return getCurrentValue().subtract(getInvestedAmount());
    }

    @Transient
    public BigDecimal getInvestedAmount() {
        return averagePurchasePrice.multiply(BigDecimal.valueOf(tokensAcquired));
    }
}