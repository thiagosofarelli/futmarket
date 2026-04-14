package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.exception.EntityNotFoundException;
import com.ar.edu.unq.futmarket.model.Portfolio;
import com.ar.edu.unq.futmarket.repositories.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public Portfolio findByUserId(Long userId) {
        return portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Portfolio not found for user: " + userId));
    }
}
