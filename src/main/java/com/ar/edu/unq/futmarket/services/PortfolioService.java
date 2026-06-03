package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.model.Portfolio;

public interface PortfolioService {

    Portfolio findByUserId(Long userId);
}
