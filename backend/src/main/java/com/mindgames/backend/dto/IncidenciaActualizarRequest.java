package com.mindgames.backend.dto;

import com.mindgames.backend.entities.EstadoIncidencia;
import com.mindgames.backend.entities.PrioridadIncidencia;

/**
 * Cuerpo de PUT /api/incidencias/{id}. Los campos en null no se cambian.
 * version es obligatoria: debe ser la versión que el cliente leyó (concurrencia optimista).
 */
public record IncidenciaActualizarRequest(String titulo, String descripcion,
                                          EstadoIncidencia estado, PrioridadIncidencia prioridad,
                                          Long version, Object creadoPor) {
}
