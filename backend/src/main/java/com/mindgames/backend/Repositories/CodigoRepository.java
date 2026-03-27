package com.mindgames.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mindgames.backend.entities.CodigoVerificacion;

public interface CodigoRepository extends JpaRepository<CodigoVerificacion, Long> {
}
