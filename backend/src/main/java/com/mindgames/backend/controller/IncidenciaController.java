package com.mindgames.backend.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindgames.backend.dto.AsignarRequest;
import com.mindgames.backend.dto.IncidenciaActualizarRequest;
import com.mindgames.backend.dto.IncidenciaCrearRequest;
import com.mindgames.backend.dto.IncidenciaResponse;
import com.mindgames.backend.dto.UsuarioResumen;
import com.mindgames.backend.services.IncidenciaService;

/**
 * /api/incidencias — todas las rutas exigen token JWT: no están en ningún
 * permitAll de SecurityConfig, así que caen en anyRequest().authenticated()
 * (sin token -> 403). La autorización por rol/dueño la aplica IncidenciaService.
 */
@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaController {

    private final IncidenciaService servicio;

    public IncidenciaController(IncidenciaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<IncidenciaResponse> crear(@RequestBody IncidenciaCrearRequest datos,
                                                    Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(datos, principal.getName()));
    }

    @GetMapping
    public List<IncidenciaResponse> listar(Principal principal) {
        return servicio.listar(principal.getName());
    }

    // Ruta fija antes que /{id} para que "agentes" no se interprete como id.
    @GetMapping("/agentes")
    public List<UsuarioResumen> agentes(Principal principal) {
        return servicio.agentes(principal.getName());
    }

    @GetMapping("/{id}")
    public IncidenciaResponse obtener(@PathVariable Long id, Principal principal) {
        return servicio.obtener(id, principal.getName());
    }

    @PutMapping("/{id}")
    public IncidenciaResponse actualizar(@PathVariable Long id,
                                         @RequestBody IncidenciaActualizarRequest datos,
                                         Principal principal) {
        return servicio.actualizar(id, datos, principal.getName());
    }

    @PutMapping("/{id}/asignar")
    public IncidenciaResponse asignar(@PathVariable Long id, @RequestBody AsignarRequest datos,
                                      Principal principal) {
        return servicio.asignar(id, datos, principal.getName());
    }

    @DeleteMapping("/{id}")
    public Map<String, String> eliminar(@PathVariable Long id, Principal principal) {
        servicio.eliminar(id, principal.getName());
        return Map.of("mensaje", "Incidencia eliminada");
    }
}
