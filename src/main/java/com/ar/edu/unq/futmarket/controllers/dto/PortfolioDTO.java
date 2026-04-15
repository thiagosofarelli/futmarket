package com.ar.edu.unq.futmarket.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDTO {
    public Long id;
    public Long userId;
    public List<PositionDTO> positions;
    public BigDecimal currentValue;
    public BigDecimal profitLoss;
}

