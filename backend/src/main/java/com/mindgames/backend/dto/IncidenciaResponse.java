package com.mindgames.backend.dto;

import java.time.LocalDateTime;

import com.mindgames.backend.entities.EstadoIncidencia;
import com.mindgames.backend.entities.Incidencia;
import com.mindgames.backend.entities.PrioridadIncidencia;

/**
 * Lo que se devuelve de una incidencia. Incluye version: el cliente debe
 * reenviarla en el PUT para detectar ediciones desde otro dispositivo.
 */
public record IncidenciaResponse(Long id, String titulo, String descripcion,
                                 EstadoIncidencia estado, PrioridadIncidencia prioridad,
                                 UsuarioResumen creadoPor, UsuarioResumen asignadoA,
                                 LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion,
                                 Long version) {

    public static IncidenciaResponse de(Incidencia i) {
        return new IncidenciaResponse(i.getId(), i.getTitulo(), i.getDescripcion(),
                i.getEstado(), i.getPrioridad(),
                UsuarioResumen.de(i.getCreadoPor()), UsuarioResumen.de(i.getAsignadoA()),
                i.getFechaCreacion(), i.getFechaActualizacion(), i.getVersion());
    }
}
