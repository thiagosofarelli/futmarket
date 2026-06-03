package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.controllers.dto.PortfolioDTO;
import com.ar.edu.unq.futmarket.controllers.dto.UserDTO;
import com.ar.edu.unq.futmarket.model.Order;
import com.ar.edu.unq.futmarket.model.Portfolio;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.services.OrderService;
import com.ar.edu.unq.futmarket.services.PortfolioService;
import com.ar.edu.unq.futmarket.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final PortfolioService portfolioService;
    private final OrderService orderService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @GetMapping("/{id}/portfolio")
    public ResponseEntity<PortfolioDTO> getPortfolio(@PathVariable Long id) {
        User user = userService.findById(id);
        PortfolioDTO dto = modelMapper.map(user.getPortfolio(), PortfolioDTO.class);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Order>> getTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getTransactionsByUserId(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        UserDTO dto = modelMapper.map(user, UserDTO.class);
        return ResponseEntity.ok(dto);
    }
}
