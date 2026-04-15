package com.ar.edu.unq.futmarket.controllers.dto;

import com.ar.edu.unq.futmarket.model.enums.OrderStatus;
import com.ar.edu.unq.futmarket.model.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    public Long id;
    public Long buyerId;
    public Long sellerId;
    public Long playerId;
    public OrderType type;
    public OrderStatus status;
    public Integer tokenQuantity;
    public BigDecimal pricePerToken;
    public BigDecimal totalAmount;
    public LocalDateTime createdAt;
    public LocalDateTime completedAt;
}

