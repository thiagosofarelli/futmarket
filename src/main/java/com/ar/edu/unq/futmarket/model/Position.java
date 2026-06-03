package com.ar.edu.unq.futmarket.model;

import com.ar.edu.unq.futmarket.exception.*;
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
            throw new InvalidTokenQuantityException();
        }
        if (pricePerToken == null || pricePerToken.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPurchasePriceException();
        }
        if (player == null) {
            throw new UserNotFoundException();
        }

        BigDecimal currentCost = averagePurchasePrice.multiply(BigDecimal.valueOf(tokensAcquired));
        BigDecimal purchaseCost = pricePerToken.multiply(BigDecimal.valueOf(tokensQuantity));

        int totalTokens = tokensAcquired + tokensQuantity;

        tokensAcquired = totalTokens;
        averagePurchasePrice = currentCost.add(purchaseCost)
                .divide(BigDecimal.valueOf(totalTokens), 4, RoundingMode.HALF_UP);
    }

    public void registerSell(int quantity) {
        if (quantity <= 0) {
            throw new InvalidTokenQuantityException();
        }
        if (quantity > tokensAcquired) {
            throw new InsufficientTokensException();
        }
        if (player == null) {
            throw new PlayerNotFoundException();
        }
        this.subTokens(quantity);
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

    public void subTokens(int tokensQuantity) {
        tokensAcquired -= tokensQuantity;
    }
}