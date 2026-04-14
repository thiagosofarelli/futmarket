package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.controllers.dto.BuyRequest;
import com.ar.edu.unq.futmarket.controllers.dto.SellRequest;
import com.ar.edu.unq.futmarket.model.Order;
import com.ar.edu.unq.futmarket.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy")
    public ResponseEntity<Order> buy(@RequestBody BuyRequest request) {
        Order order = orderService.buy(request.getBuyerId(), request.getPlayerId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/sell")
    public ResponseEntity<Order> sell(@RequestBody SellRequest request) {
        Order order = orderService.sell(request.getSellerId(), request.getPlayerId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}
