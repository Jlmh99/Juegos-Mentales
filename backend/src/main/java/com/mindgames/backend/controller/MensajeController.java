package com.mindgames.backend.controller;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Duration;
import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mindgames.backend.dto.MensajeRequest;
import com.mindgames.backend.dto.MensajeResponse;
import com.mindgames.backend.entities.CanalMensaje;
import com.mindgames.backend.services.MensajeService;

/**
 * /api/incidencias/{id}/mensajes — exige token JWT.
 * ?canal=PUBLICO (por defecto) o ?canal=INTERNO (solo agente asignado y ADMIN).
 * El listado NO incluye el contenido de los adjuntos: se pide con GET /{mid}/adjunto.
 */
@RestController
@RequestMapping("/api/incidencias/{id}/mensajes")
public class MensajeController {

    private final MensajeService servicio;

    public MensajeController(MensajeService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<MensajeResponse> listar(@PathVariable Long id,
                                        @RequestParam(defaultValue = "PUBLICO") CanalMensaje canal,
                                        Principal principal) {
        return servicio.listar(id, canal, principal.getName());
    }

    @PostMapping
    public ResponseEntity<MensajeResponse> enviar(@PathVariable Long id, @RequestBody MensajeRequest datos,
                                                  Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.enviar(id, datos, principal.getName()));
    }

    /**
     * Archivo adjunto de un mensaje, en binario (no Base64). Mismos permisos y
     * canales que el listado; ver MensajeService.adjunto().
     * Los adjuntos no se editan, por eso se permite guardarlos en caché privada 1 día.
     */
    @GetMapping("/{mid}/adjunto")
    public ResponseEntity<byte[]> adjunto(@PathVariable Long id, @PathVariable Long mid, Principal principal) {
        MensajeService.ArchivoAdjunto a = servicio.adjunto(id, mid, principal.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.tipo()))
                .contentLength(a.bytes().length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(a.nombre(), StandardCharsets.UTF_8).build().toString())
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate())
                .header("X-Content-Type-Options", "nosniff")
                .body(a.bytes());
    }
}
