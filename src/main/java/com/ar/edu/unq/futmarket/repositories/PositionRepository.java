package com.ar.edu.unq.futmarket.repositories;

import com.ar.edu.unq.futmarket.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {
}
