package com.mindgames.backend.dto;

/**
 * Datos de un adjunto SIN su contenido (Práctica 12).
 * El archivo se descarga aparte con GET /api/incidencias/{id}/mensajes/{mid}/adjunto.
 *  - tamano: bytes reales del archivo (columna adjunto_tamano).
 *    Es 0 en un mensaje anterior a esa columna que todavía no pasó por
 *    pruebas/backfill_adjunto_tamano.sql (el cliente solo lo usa para mostrarlo).
 */
public record AdjuntoInfo(String nombre, String tipo, long tamano) {

    /** null (mensaje antiguo sin backfill) se devuelve como 0. */
    public static long tamanoOCero(Long tamano) {
        return tamano == null ? 0 : tamano;
    }
}
