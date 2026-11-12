package com.mindgames.backend.dto;

import java.time.LocalDateTime;

import com.mindgames.backend.entities.CanalMensaje;

/**
 * Fila del listado de mensajes que arma MensajeRepository.listarSinContenido().
 * Trae el largo del Base64 y sus 2 últimos caracteres en lugar del contenido,
 * para que PostgreSQL no envíe los adjuntos en cada recarga del chat (Práctica 12).
 */
public record MensajeFila(Long id, Long incidenciaId,
                          Long emisorId, String emisorUsername, String emisorEmail, String emisorRol,
                          String texto, CanalMensaje canal, LocalDateTime fecha,
                          String adjuntoNombre, String adjuntoTipo,
                          Integer adjuntoLargo, String adjuntoUltimos) {
}
