package com.ar.edu.unq.futmarket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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
@AllArgsConstructor
@Builder
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private int tokensAcquired;

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal averagePurchasePrice = BigDecimal.ZERO;

    @Version
    private Long version;

    public void registerPurchase(int tokensQuantity, BigDecimal pricePerToken) {
        if (tokensQuantity <= 0) {
            throw new IllegalArgumentException("token quantity must be greater than zero");
        }
        if (pricePerToken == null || pricePerToken.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("purchase price must be greater than zero");
        }
        if (player == null) {
            throw new IllegalStateException("position must be associated with a player");
        }

        BigDecimal currentCost = averagePurchasePrice.multiply(BigDecimal.valueOf(tokensAcquired));
        BigDecimal purchaseCost = pricePerToken.multiply(BigDecimal.valueOf(tokensQuantity));
        int totalTokens = tokensAcquired + tokensQuantity;

        tokensAcquired = totalTokens;
        averagePurchasePrice = currentCost.add(purchaseCost)
                .divide(BigDecimal.valueOf(totalTokens), 4, RoundingMode.HALF_UP);
        player.subAvailableTokens(tokensQuantity);
    }

    public void registerSell(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("token quantity must be greater than zero");
        }
        if (quantity > tokensAcquired) {
            throw new IllegalArgumentException("cannot sell more tokens than currently held");
        }
        if (player == null) {
            throw new IllegalStateException("position must be associated with a player");
        }

        tokensAcquired -= quantity;
        if (this.tokensAcquired == 0) {
            this.averagePurchasePrice = BigDecimal.ZERO;
        }
        player.addAvailableTokens(quantity);
    }

    public BigDecimal getCurrentValue() {
        if (player == null || player.getCurrentTokenPrice() == null) {
            return BigDecimal.ZERO;
        }
        return player.getCurrentTokenPrice().multiply(BigDecimal.valueOf(tokensAcquired));
    }

    public BigDecimal getProfitLoss() {
        return getCurrentValue().subtract(getInvestedAmount());
    }

    public BigDecimal getInvestedAmount() {
        return averagePurchasePrice.multiply(BigDecimal.valueOf(tokensAcquired));
    }
}