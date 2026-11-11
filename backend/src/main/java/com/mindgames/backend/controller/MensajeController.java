package com.mindgames.backend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
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
}
