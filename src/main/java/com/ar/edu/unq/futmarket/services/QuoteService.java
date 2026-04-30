package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.Quote;
import com.ar.edu.unq.futmarket.model.enums.ValuationStrategy;

import java.util.List;

public interface QuoteService {

    public List<Quote> findByPlayerId(Long playerId);

    public void recalculateSinglePlayer(Player player, ValuationStrategy strategy);

    public void recalculateAll(ValuationStrategy strategy);

    public void scheduleWeeklyRecalculation();

}
