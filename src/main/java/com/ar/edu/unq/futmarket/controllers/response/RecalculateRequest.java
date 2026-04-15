package com.ar.edu.unq.futmarket.controllers.response;

import com.ar.edu.unq.futmarket.model.enums.ValuationStrategy;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecalculateRequest {
    private ValuationStrategy strategy;
}
