package com.ar.edu.unq.futmarket.controllers.dto;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioDTO {
    public Long id;
    public Long userId;
    public List<PositionDTO> positions;
    public BigDecimal currentValue;
    public BigDecimal profitLoss;
}

