package com.mindgames.backend.dto;

import com.mindgames.backend.entities.PrioridadIncidencia;

/**
 * Cuerpo de POST /api/incidencias.
 * creadoPor es opcional y solo se usa para RECHAZAR (403) si no coincide con el
 * usuario del token; el creador real siempre se toma del token.
 * Puede venir como email, como id o como objeto {"id": .., "email": ..}.
 */
public record IncidenciaCrearRequest(String titulo, String descripcion,
                                     PrioridadIncidencia prioridad, Object creadoPor) {
}
