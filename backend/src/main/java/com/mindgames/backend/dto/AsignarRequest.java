package com.mindgames.backend.dto;

/** Cuerpo de PUT /api/incidencias/{id}/asignar: id de un usuario con rol AGENTE. */
public record AsignarRequest(Long agenteId) {
}
