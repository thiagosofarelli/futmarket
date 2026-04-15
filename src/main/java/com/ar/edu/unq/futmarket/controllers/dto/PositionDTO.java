package com.ar.edu.unq.futmarket.controllers.dto;

import java.math.BigDecimal;

public class PositionDTO {
    public Long id;
    public Long playerId;
    public Integer tokensAcquired;
    public BigDecimal averagePurchasePrice;
    public BigDecimal currentValue;
    public BigDecimal profitLoss;
}

