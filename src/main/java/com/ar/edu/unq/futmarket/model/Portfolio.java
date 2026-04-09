package com.ar.edu.unq.futmarket.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "portfolios")
@Getter
@Setter
@NoArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "portfolio", fetch = FetchType.LAZY)
    private User user;

    // mappedBy apunta al nombre del atributo en la clase Position
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Position> positions = new ArrayList<>();

    @Version
    private Long version;

    public Optional<Position> getPosition(Player player) {
        return positions.stream()
                .filter(position -> samePlayer(position.getPlayer(), player))
                .findFirst();
    }

    public Position addOrUpdatePosition(Player player, int tokensAcquired, BigDecimal purchasePricePerToken) {
        Position position = getPosition(player).orElseGet(() -> {
            Position created = new Position();
            created.setPortfolio(this);
            created.setPlayer(player);
            positions.add(created);
            return created;
        });

        position.registerPurchase(tokensAcquired, purchasePricePerToken);
        return position;
    }

    public void registerSale(Player player, int tokensSold) {
        Position position = getPosition(player)
                .orElseThrow(() -> new IllegalArgumentException("The portfolio does not contain a position for the given player"));

        position.registerSale(tokensSold);

        if (position.getTokensAcquired() == 0) {
            positions.remove(position);
        }
    }

    public BigDecimal calculateCurrentValue(Player player) {
        return getPosition(player)
                .map(Position::getCurrentValue)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal calculateProfitLoss(Player player) {
        return getPosition(player)
                .map(Position::getProfitLoss)
                .orElse(BigDecimal.ZERO);
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
}
