package com.futmarket.futmarket.quotes;

import com.futmarket.futmarket.players.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByPlayerOrderByCalculatedAtDesc(Player player);

    List<Quote> findByPlayerIdOrderByCalculatedAtDesc(Long playerId);
}
