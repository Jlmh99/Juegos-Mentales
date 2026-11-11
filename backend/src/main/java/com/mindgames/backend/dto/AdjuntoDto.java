package com.mindgames.backend.dto;

/**
 * Adjunto de un mensaje.
 *  - nombre: nombre del archivo (ej. "captura.png").
 *  - tipo: image/jpeg, image/png o application/pdf.
 *  - contenido: el archivo en Base64 (máximo 2 MB ya decodificado).
 */
public record AdjuntoDto(String nombre, String tipo, String contenido) {
}
