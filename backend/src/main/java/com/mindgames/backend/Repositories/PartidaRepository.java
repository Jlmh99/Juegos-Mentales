package com.mindgames.backend.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mindgames.backend.entities.Partida;
import com.mindgames.backend.entities.Usuario;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {

    /**
     * Partidas de un usuario, de la más reciente a la más antigua.
     * Si dos partidas tienen la misma fecha, va primero la de id mayor (la última guardada).
     */
    List<Partida> findByUsuarioOrderByFechaDescIdDesc(Usuario usuario);
}
