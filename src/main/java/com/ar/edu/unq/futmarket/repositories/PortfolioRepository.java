package com.ar.edu.unq.futmarket.repositories;

import com.ar.edu.unq.futmarket.model.Portfolio;
import com.ar.edu.unq.futmarket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByUser(User user);

    Optional<Portfolio> findByUserId(Long userId);
}
