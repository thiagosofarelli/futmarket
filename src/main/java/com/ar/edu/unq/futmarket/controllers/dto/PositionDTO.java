package com.ar.edu.unq.futmarket.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PositionDTO {
    public Long id;
    public Long playerId;
    public Integer tokensAcquired;
    public BigDecimal averagePurchasePrice;
    public BigDecimal currentValue;
    public BigDecimal profitLoss;
}

