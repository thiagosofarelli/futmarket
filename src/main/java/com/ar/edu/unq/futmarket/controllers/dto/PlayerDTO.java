package com.ar.edu.unq.futmarket.controllers.dto;

import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    public Long id;
    public String name;
    public String league;
    public String team;
    public PlayerPosition playerPosition;
    public BigDecimal currentTokenPrice;
    public Double goals;
    public Double assists;
    public Double shots;
    public Double keyPasses;
    public Double dribbles;
    public Double tackles;
    public Double interceptions;
    public Double rating;
    public Integer availableTokens;
    public String externalId;
}

