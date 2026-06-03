package com.ar.edu.unq.futmarket.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "portfolios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "portfolio")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Position> positions = new ArrayList<>();

    @Version
    private Long version;

    public BigDecimal getCurrentValue() {
        return positions.stream()
                .map(Position::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getProfitLoss() {
        return positions.stream()
                .map(Position::getProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Optional<Position> getPosition(Player player) {
        return positions.stream()
                .filter(p -> samePlayer(p.getPlayer(), player))
                .findFirst();
    }

    private boolean samePlayer(Player left, Player right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.getId() != null && left.getId().equals(right.getId());
    }

    public void registerPurchase(Player player, int tokensQuantity, User superuser) {
        validateTradeInput(player, tokensQuantity);
        ensureUserAssigned();

        BigDecimal pricePerToken = requirePositivePrice(player);
        BigDecimal totalCost = pricePerToken.multiply(BigDecimal.valueOf(tokensQuantity));

        if (user.getBalance().compareTo(totalCost) < 0) {
            throw new IllegalArgumentException("You don't have enough balance.");
        }
        Position superuserPosition = superuser.getPortfolio().getPosition(player).get();

        superuserPosition.registerSell(tokensQuantity);
        if (superuserPosition.getTokensAcquired() == 0) {
            superuser.getPortfolio().getPositions().remove(superuserPosition);
        }
        Position position = findOrCreatePosition(player);
        position.registerPurchase(tokensQuantity, pricePerToken);
        user.subBalance(totalCost);
        superuser.addBalance(totalCost);
    }

    public void registerSell(Player player, int tokensQuantity, User superuser) {
        validateTradeInput(player, tokensQuantity);
        ensureUserAssigned();

        BigDecimal pricePerToken = requirePositivePrice(player);
        Position position = this.getPosition(player)
                .orElseThrow(() -> new IllegalArgumentException("The seller portfolio does not have that position."));

        position.registerSell(tokensQuantity);

        Position superUserPosition = superuser.getPortfolio().findOrCreatePosition(player);
        superUserPosition.registerPurchase(tokensQuantity, pricePerToken);

        if (position.getTokensAcquired() == 0) {
            positions.remove(position);
        }

        user.addBalance(pricePerToken.multiply(BigDecimal.valueOf(tokensQuantity)));
        superuser.subBalance(pricePerToken.multiply(BigDecimal.valueOf(tokensQuantity)));
    }

    private void validateTradeInput(Player player, int tokensQuantity) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }
        if (tokensQuantity <= 0) {
            throw new IllegalArgumentException("Token quantity must be greater than zero");
        }
    }

    private void ensureUserAssigned() {
        if (user == null) {
            throw new IllegalStateException("Portfolio must be associated with a user");
        }
    }

    private BigDecimal requirePositivePrice(Player player) {
        BigDecimal pricePerToken = player.getCurrentTokenPrice();
        if (pricePerToken == null || pricePerToken.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Player current token price must be greater than zero.");
        }
        return pricePerToken;
    }

    private Position findOrCreatePosition(Player player) {
        return getPosition(player)
                .orElseGet(() -> {
                    Position newPosition = new Position();
                    newPosition.setPortfolio(this);
                    newPosition.setPlayer(player);
                    positions.add(newPosition);
                    return newPosition;
                });
    }
}
