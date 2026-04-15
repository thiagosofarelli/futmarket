package com.ar.edu.unq.futmarket.controllers.dto;

import java.math.BigDecimal;

public class UserDTO {
    public Long id;
    public String username;
    public BigDecimal balance;
    public boolean superuser;
    public PortfolioDTO portfolio;
}

