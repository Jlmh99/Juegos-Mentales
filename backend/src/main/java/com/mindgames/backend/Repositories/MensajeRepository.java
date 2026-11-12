package com.mindgames.backend.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mindgames.backend.dto.MensajeFila;
import com.mindgames.backend.entities.CanalMensaje;
import com.mindgames.backend.entities.Incidencia;
import com.mindgames.backend.entities.Mensaje;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    /**
     * Mensajes de un canal de la incidencia, del más antiguo al más reciente (orden de chat),
     * SIN tocar adjunto_contenido (Práctica 12, Fase 6b): solo columnas pequeñas
     * (nombre, tipo y adjunto_tamano). Antes se usaba length()/substring() sobre el
     * TEXT y PostgreSQL tenía que leer todos los adjuntos en cada recarga del chat.
     * Consulta parametrizada (:incidencia, :canal), sin concatenar datos.
     */
    @Query("""
            select new com.mindgames.backend.dto.MensajeFila(
                m.id, m.incidencia.id,
                e.id, e.username, e.email, e.rol,
                m.texto, m.canal, m.fecha,
                m.adjuntoNombre, m.adjuntoTipo, m.adjuntoTamano)
            from Mensaje m join m.emisor e
            where m.incidencia = :incidencia and m.canal = :canal
            order by m.fecha asc, m.id asc
            """)
    List<MensajeFila> listarSinContenido(@Param("incidencia") Incidencia incidencia,
                                         @Param("canal") CanalMensaje canal);

    /** Borra todos los mensajes de una incidencia (se usa al eliminarla). */
    void deleteByIncidencia(Incidencia incidencia);
}
