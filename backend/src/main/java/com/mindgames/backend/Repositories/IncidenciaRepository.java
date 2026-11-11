package com.mindgames.backend.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mindgames.backend.entities.Incidencia;
import com.mindgames.backend.entities.Usuario;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    /** Incidencias creadas por un usuario (vista de USER), de la más reciente a la más antigua. */
    List<Incidencia> findByCreadoPorOrderByFechaCreacionDesc(Usuario creadoPor);

    /** Incidencias asignadas a un agente (vista de AGENTE). */
    List<Incidencia> findByAsignadoAOrderByFechaCreacionDesc(Usuario asignadoA);

    /** Todas las incidencias (vista de ADMIN). */
    List<Incidencia> findAllByOrderByFechaCreacionDesc();
}
