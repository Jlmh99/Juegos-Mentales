package com.mindgames.backend.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindgames.backend.Repositories.PartidaRepository;
import com.mindgames.backend.Repositories.UsuarioRepository;
import com.mindgames.backend.dto.PartidaRequest;
import com.mindgames.backend.dto.PartidaResponse;
import com.mindgames.backend.entities.Partida;
import com.mindgames.backend.entities.Usuario;

/**
 * Partidas de los minijuegos.
 * Seguridad: /api/partidas no está en ninguna regla permitAll de SecurityConfig,
 * así que cae en anyRequest().authenticated() y exige el token JWT.
 * El usuario se obtiene del token (JwtFilter pone el email como Principal),
 * nunca del cuerpo de la petición.
 */
@RestController
@RequestMapping("/api/partidas")
public class PartidaController {

    private static final Set<String> JUEGOS_VALIDOS = Set.of("SUDOKU", "CRUCIGRAMA");

    private final PartidaRepository repo;
    private final UsuarioRepository repoUsuarios;

    public PartidaController(PartidaRepository repo, UsuarioRepository repoUsuarios) {
        this.repo = repo;
        this.repoUsuarios = repoUsuarios;
    }

    /** Guarda una partida terminada del usuario autenticado. */
    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody PartidaRequest datos, Principal principal) {
        Optional<Usuario> usuario = repoUsuarios.findByEmail(principal.getName());
        if (usuario.isEmpty()) {
            // Token válido pero el usuario ya no existe: se trata como sesión inválida.
            return mensaje(HttpStatus.UNAUTHORIZED, "Sesión no válida");
        }

        if (datos == null || datos.juego() == null || !JUEGOS_VALIDOS.contains(datos.juego())) {
            return mensaje(HttpStatus.BAD_REQUEST, "Juego no válido");
        }
        if (datos.tiempoSegundos() == null || datos.tiempoSegundos() < 0
                || datos.puntuacion() == null || datos.puntuacion() < 0) {
            return mensaje(HttpStatus.BAD_REQUEST, "Tiempo y puntuación deben ser mayores o iguales a 0");
        }

        Partida partida = new Partida();
        partida.setUsuario(usuario.get());
        partida.setJuego(datos.juego());
        partida.setTiempoSegundos(datos.tiempoSegundos());
        partida.setPuntuacion(datos.puntuacion());
        partida.setFecha(LocalDateTime.now()); // la fecha la pone el servidor
        repo.save(partida);

        return ResponseEntity.status(HttpStatus.CREATED).body(PartidaResponse.de(partida));
    }

    /** Partidas del usuario autenticado, de la más reciente a la más antigua. */
    @GetMapping("/me")
    public ResponseEntity<?> misPartidas(Principal principal) {
        Optional<Usuario> usuario = repoUsuarios.findByEmail(principal.getName());
        if (usuario.isEmpty()) {
            return mensaje(HttpStatus.UNAUTHORIZED, "Sesión no válida");
        }

        List<PartidaResponse> partidas = repo.findByUsuarioOrderByFechaDescIdDesc(usuario.get())
                .stream()
                .map(PartidaResponse::de)
                .toList();
        return ResponseEntity.ok(partidas);
    }

    /** Respuesta JSON genérica { "mensaje": "..." } sin detalles técnicos. */
    private ResponseEntity<Map<String, String>> mensaje(HttpStatus status, String texto) {
        return ResponseEntity.status(status).body(Map.of("mensaje", texto));
    }
}
