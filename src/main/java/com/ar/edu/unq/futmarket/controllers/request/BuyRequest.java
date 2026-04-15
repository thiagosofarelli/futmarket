package com.ar.edu.unq.futmarket.controllers.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BuyRequest {
    private Long playerId;
    private int quantity;
}
