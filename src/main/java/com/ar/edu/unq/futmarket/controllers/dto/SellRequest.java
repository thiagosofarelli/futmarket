package com.ar.edu.unq.futmarket.controllers.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SellRequest {
    private Long playerId;
    private int quantity;
}
