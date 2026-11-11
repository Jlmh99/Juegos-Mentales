package com.mindgames.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mindgames.backend.entities.Soporte;

@Repository
public interface SoporteRepository extends JpaRepository<Soporte, Long> {
}
