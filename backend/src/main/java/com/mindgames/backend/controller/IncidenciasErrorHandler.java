package com.mindgames.backend.controller;

import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.mindgames.backend.services.ErrorApi;

/**
 * Convierte los errores de incidencias y mensajes en respuestas JSON
 * { "mensaje": "..." } con mensajes GENÉRICOS (sin trazas ni detalles técnicos).
 *
 * Solo aplica a IncidenciaController y MensajeController: los demás controllers
 * del proyecto se comportan igual que antes.
 * No se registra nada en logs (ni tokens, ni cuerpos de petición).
 */
@RestControllerAdvice(assignableTypes = { IncidenciaController.class, MensajeController.class })
public class IncidenciasErrorHandler {

    /** Errores de negocio: 400, 401, 403, 404 o 409 con su mensaje genérico. */
    @ExceptionHandler(ErrorApi.class)
    public ResponseEntity<Map<String, String>> errorApi(ErrorApi e) {
        return respuesta(e.getStatus(), e.getMessage());
    }

    /** Dos guardados simultáneos de la misma versión (lo detecta JPA con @Version). */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> conflicto() {
        return respuesta(HttpStatus.CONFLICT, "El registro fue modificado desde otro dispositivo");
    }

    /** JSON mal formado, enum desconocido (ej. estado "XYZ") o id no numérico. */
    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<Map<String, String>> datosInvalidos() {
        return respuesta(HttpStatus.BAD_REQUEST, "Datos no válidos");
    }

    /** Cualquier otro error inesperado: mensaje genérico, sin detalles. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> inesperado() {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error, intenta de nuevo");
    }

    private static ResponseEntity<Map<String, String>> respuesta(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(Map.of("mensaje", mensaje));
    }
}
