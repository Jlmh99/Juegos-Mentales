package com.mindgames.backend.services;

import org.springframework.http.HttpStatus;

/**
 * Error de negocio con su código HTTP y un mensaje GENÉRICO para el cliente
 * (sin detalles técnicos). Lo convierte en respuesta IncidenciasErrorHandler.
 */
public class ErrorApi extends RuntimeException {

    private final HttpStatus status;

    public ErrorApi(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    // Atajos para los casos más comunes.
    public static ErrorApi prohibido() {
        return new ErrorApi(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción");
    }

    public static ErrorApi noEncontrada() {
        return new ErrorApi(HttpStatus.NOT_FOUND, "Incidencia no encontrada");
    }

    public static ErrorApi datosInvalidos(String mensaje) {
        return new ErrorApi(HttpStatus.BAD_REQUEST, mensaje);
    }

    public static ErrorApi conflictoVersion() {
        return new ErrorApi(HttpStatus.CONFLICT, "El registro fue modificado desde otro dispositivo");
    }
}
