package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.controllers.dto.PortfolioDTO;
import com.ar.edu.unq.futmarket.model.Portfolio;
import com.ar.edu.unq.futmarket.services.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final ModelMapper modelMapper;

    @GetMapping("/user/{userId}")
    public ResponseEntity<PortfolioDTO> getPortfolioByUserId(@PathVariable Long userId) {
        Portfolio portfolio = portfolioService.findByUserId(userId);
        PortfolioDTO dto = modelMapper.map(portfolio, PortfolioDTO.class);
        return ResponseEntity.ok(dto);
    }
}

