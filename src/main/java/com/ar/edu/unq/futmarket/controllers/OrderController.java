package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.controllers.dto.OrderDTO;
import com.ar.edu.unq.futmarket.controllers.request.BuyRequest;
import com.ar.edu.unq.futmarket.controllers.request.SellRequest;
import com.ar.edu.unq.futmarket.model.Order;
import com.ar.edu.unq.futmarket.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ModelMapper modelMapper;

    @PostMapping("/buy")
    public ResponseEntity<OrderDTO> buy(@RequestBody BuyRequest request,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        Order order = orderService.buy(userDetails, request.getPlayerId(), request.getQuantity());
        OrderDTO dto = modelMapper.map(order, OrderDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/sell")
    public ResponseEntity<OrderDTO> sell(@RequestBody SellRequest request,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        Order order = orderService.sell(userDetails, request.getPlayerId(), request.getQuantity());
        OrderDTO dto = modelMapper.map(order, OrderDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable Long userId) {
        List<Order> orders = orderService.getTransactionsByUserId(userId);
        List<OrderDTO> dtos = orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
