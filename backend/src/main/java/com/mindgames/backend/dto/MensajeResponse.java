package com.mindgames.backend.dto;

import java.time.LocalDateTime;

import com.mindgames.backend.entities.CanalMensaje;
import com.mindgames.backend.entities.Mensaje;

/**
 * Lo que se devuelve de un mensaje. Desde la Práctica 12 NO incluye el Base64
 * del adjunto: solo tieneAdjunto y sus datos (nombre, tipo, tamaño).
 * El archivo se pide con GET /api/incidencias/{id}/mensajes/{mid}/adjunto.
 */
public record MensajeResponse(Long id, Long incidenciaId, UsuarioResumen emisor, String texto,
                              CanalMensaje canal, LocalDateTime fecha,
                              boolean tieneAdjunto, AdjuntoInfo adjunto) {

    /** Desde la entidad (respuesta del POST): el contenido ya está en memoria. */
    public static MensajeResponse de(Mensaje m) {
        String c = m.getAdjuntoContenido();
        AdjuntoInfo adjunto = c == null ? null
                : new AdjuntoInfo(m.getAdjuntoNombre(), m.getAdjuntoTipo(),
                        AdjuntoInfo.tamanoDesdeBase64(c.length(), c.substring(Math.max(0, c.length() - 2))));
        return new MensajeResponse(m.getId(), m.getIncidencia().getId(), UsuarioResumen.de(m.getEmisor()),
                m.getTexto(), m.getCanal(), m.getFecha(), adjunto != null, adjunto);
    }

    /** Desde la fila del listado (sin contenido). */
    public static MensajeResponse de(MensajeFila f) {
        AdjuntoInfo adjunto = f.adjuntoLargo() == null ? null
                : new AdjuntoInfo(f.adjuntoNombre(), f.adjuntoTipo(),
                        AdjuntoInfo.tamanoDesdeBase64(f.adjuntoLargo(), f.adjuntoUltimos()));
        UsuarioResumen emisor = new UsuarioResumen(f.emisorId(), f.emisorUsername(), f.emisorEmail(), f.emisorRol());
        return new MensajeResponse(f.id(), f.incidenciaId(), emisor, f.texto(), f.canal(), f.fecha(),
                adjunto != null, adjunto);
    }
}
