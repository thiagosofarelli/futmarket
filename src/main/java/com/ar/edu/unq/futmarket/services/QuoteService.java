package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.Quote;
import com.ar.edu.unq.futmarket.model.enums.ValuationStrategy;

import java.util.List;

public interface QuoteService {

    List<Quote> findByPlayerId(Long playerId);

    void recalculateSinglePlayer(Player player, ValuationStrategy strategy);

    void recalculateAll(ValuationStrategy strategy);

    void scheduleWeeklyRecalculation();

}
