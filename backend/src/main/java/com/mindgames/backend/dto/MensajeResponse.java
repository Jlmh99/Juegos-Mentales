package com.mindgames.backend.dto;

import java.time.LocalDateTime;

import com.mindgames.backend.entities.CanalMensaje;
import com.mindgames.backend.entities.Mensaje;

/** Lo que se devuelve de un mensaje (adjunto en null si no tiene). */
public record MensajeResponse(Long id, Long incidenciaId, UsuarioResumen emisor, String texto,
                              CanalMensaje canal, LocalDateTime fecha, AdjuntoDto adjunto) {

    public static MensajeResponse de(Mensaje m) {
        AdjuntoDto adjunto = m.getAdjuntoContenido() == null ? null
                : new AdjuntoDto(m.getAdjuntoNombre(), m.getAdjuntoTipo(), m.getAdjuntoContenido());
        return new MensajeResponse(m.getId(), m.getIncidencia().getId(), UsuarioResumen.de(m.getEmisor()),
                m.getTexto(), m.getCanal(), m.getFecha(), adjunto);
    }
}
