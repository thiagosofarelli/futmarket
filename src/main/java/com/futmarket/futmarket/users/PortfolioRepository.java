package com.futmarket.futmarket.users;

import com.futmarket.futmarket.players.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByUser(User user);

    List<Portfolio> findByUserId(Long userId);

    Optional<Portfolio> findByUserAndPlayer(User user, Player player);

    Optional<Portfolio> findByUserIdAndPlayerId(Long userId, Long playerId);
}
