package com.ar.edu.unq.futmarket.services.impl;

import com.ar.edu.unq.futmarket.exception.PortfolioNotFoundException;
import com.ar.edu.unq.futmarket.model.Portfolio;
import com.ar.edu.unq.futmarket.repositories.PortfolioRepository;
import com.ar.edu.unq.futmarket.services.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public Portfolio findByUserId(Long userId) {
        return portfolioRepository.findByUserId(userId)
                .orElseThrow(PortfolioNotFoundException::new);
    }
}
