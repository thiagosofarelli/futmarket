package com.ar.edu.unq.futmarket.controllers.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BuyRequest {
    private Long playerId;
    private int quantity;
}
