package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.model.Portfolio;

public interface PortfolioService {

    public Portfolio findByUserId(Long userId);
}
