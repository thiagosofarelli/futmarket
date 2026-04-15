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
    public Integer goals;
    public Integer assists;
    public Integer shots;
    public Integer keyPasses;
    public Integer dribbles;
    public Integer tackles;
    public Integer interceptions;
    public Double rating;
    public Integer availableTokens;
    public String externalId;
}

