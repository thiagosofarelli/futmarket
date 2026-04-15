package com.ar.edu.unq.futmarket.controllers.dto;

import com.ar.edu.unq.futmarket.model.enums.ValuationStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuoteDTO {
    public Long id;
    public Long playerId;
    public BigDecimal currentTokenPrice;
    public ValuationStrategy strategy;
    public Double score;
    public LocalDateTime calculatedAt;
}

