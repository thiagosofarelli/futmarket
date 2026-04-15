package com.ar.edu.unq.futmarket.controllers.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    public Long id;
    public String username;
    public BigDecimal balance;
    public boolean superuser;
    public PortfolioDTO portfolio;
}

