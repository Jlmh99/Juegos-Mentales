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

    // Un mensaje tiene adjunto si tiene nombre de adjunto: aplicarAdjunto() siempre
    // guarda nombre, tipo, contenido y tamaño juntos (el nombre nunca queda vacío).

    /** Desde la entidad (respuesta del POST). */
    public static MensajeResponse de(Mensaje m) {
        AdjuntoInfo adjunto = m.getAdjuntoNombre() == null ? null
                : new AdjuntoInfo(m.getAdjuntoNombre(), m.getAdjuntoTipo(),
                        AdjuntoInfo.tamanoOCero(m.getAdjuntoTamano()));
        return new MensajeResponse(m.getId(), m.getIncidencia().getId(), UsuarioResumen.de(m.getEmisor()),
                m.getTexto(), m.getCanal(), m.getFecha(), adjunto != null, adjunto);
    }

    /** Desde la fila del listado (sin contenido). tamano = 0 si el mensaje es antiguo y no tiene backfill. */
    public static MensajeResponse de(MensajeFila f) {
        AdjuntoInfo adjunto = f.adjuntoNombre() == null ? null
                : new AdjuntoInfo(f.adjuntoNombre(), f.adjuntoTipo(),
                        AdjuntoInfo.tamanoOCero(f.adjuntoTamano()));
        UsuarioResumen emisor = new UsuarioResumen(f.emisorId(), f.emisorUsername(), f.emisorEmail(), f.emisorRol());
        return new MensajeResponse(f.id(), f.incidenciaId(), emisor, f.texto(), f.canal(), f.fecha(),
                adjunto != null, adjunto);
    }
}
