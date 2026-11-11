package com.mindgames.backend.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindgames.backend.Repositories.SoporteRepository;
import com.mindgames.backend.entities.Soporte;

@RestController
@RequestMapping("/api/soporte")
public class SoporteController {

    private final SoporteRepository repo;

    public SoporteController(SoporteRepository repo) {
        this.repo = repo;
    }

    // Público: cualquier visitante (logueado o no) puede mandar una solicitud de ayuda
    @PostMapping
    public ResponseEntity<?> enviar(@RequestBody Soporte solicitud) {

        if (solicitud.getNombre() == null || solicitud.getNombre().isBlank()
                || solicitud.getCorreo() == null || solicitud.getCorreo().isBlank()
                || solicitud.getMensaje() == null || solicitud.getMensaje().isBlank()) {
            return ResponseEntity.badRequest().body("Nombre, correo y mensaje son obligatorios");
        }

        solicitud.setId(null);
        solicitud.setFechaEnvio(LocalDateTime.now());
        repo.save(solicitud);

        return ResponseEntity.ok("Tu solicitud fue enviada. Nuestro equipo te contactará pronto.");
    }
}
