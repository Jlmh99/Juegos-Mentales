package com.mindgames.backend.dto;

/**
 * Datos de un adjunto SIN su contenido (Práctica 12).
 * El archivo se descarga aparte con GET /api/incidencias/{id}/mensajes/{mid}/adjunto.
 *  - tamano: bytes reales del archivo (ya decodificado).
 */
public record AdjuntoInfo(String nombre, String tipo, long tamano) {

    /**
     * Bytes reales a partir del largo del Base64 y sus 2 últimos caracteres
     * (cada '=' final es un byte de relleno). No decodifica nada.
     */
    public static long tamanoDesdeBase64(int largo, String ultimos) {
        if (largo <= 0) return 0;
        int relleno = 0;
        if (ultimos != null) {
            if (ultimos.endsWith("==")) relleno = 2;
            else if (ultimos.endsWith("=")) relleno = 1;
        }
        return (long) largo / 4 * 3 - relleno;
    }
}
