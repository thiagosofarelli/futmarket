package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.model.Order;
import com.ar.edu.unq.futmarket.model.Portfolio;
import com.ar.edu.unq.futmarket.services.OrderService;
import com.ar.edu.unq.futmarket.services.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final PortfolioService portfolioService;
    private final OrderService orderService;

    @GetMapping("/{id}/portfolio")
    public ResponseEntity<Portfolio> getPortfolio(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.findByUserId(id));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Order>> getTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getTransactionsByUserId(id));
    }
}
