package com.mindgames.backend.dto;

import java.time.LocalDateTime;

import com.mindgames.backend.entities.CanalMensaje;

/**
 * Fila del listado de mensajes que arma MensajeRepository.listarSinContenido().
 * Solo columnas pequeñas: del adjunto trae nombre, tipo y tamaño (adjunto_tamano),
 * nunca adjunto_contenido. Así PostgreSQL no lee los adjuntos en cada recarga
 * del chat (Práctica 12, Fase 6b).
 */
public record MensajeFila(Long id, Long incidenciaId,
                          Long emisorId, String emisorUsername, String emisorEmail, String emisorRol,
                          String texto, CanalMensaje canal, LocalDateTime fecha,
                          String adjuntoNombre, String adjuntoTipo, Long adjuntoTamano) {
}
