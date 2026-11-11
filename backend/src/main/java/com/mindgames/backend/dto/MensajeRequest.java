package com.mindgames.backend.dto;

import com.mindgames.backend.entities.CanalMensaje;

/** Cuerpo de POST /api/incidencias/{id}/mensajes. Si canal no se envía, se usa PUBLICO. */
public record MensajeRequest(String texto, CanalMensaje canal, AdjuntoDto adjunto) {
}
