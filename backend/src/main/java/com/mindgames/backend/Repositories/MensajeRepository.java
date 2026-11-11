package com.mindgames.backend.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mindgames.backend.entities.CanalMensaje;
import com.mindgames.backend.entities.Incidencia;
import com.mindgames.backend.entities.Mensaje;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    /** Mensajes de un canal de la incidencia, del más antiguo al más reciente (orden de chat). */
    List<Mensaje> findByIncidenciaAndCanalOrderByFechaAscIdAsc(Incidencia incidencia, CanalMensaje canal);

    /** Borra todos los mensajes de una incidencia (se usa al eliminarla). */
    void deleteByIncidencia(Incidencia incidencia);
}
