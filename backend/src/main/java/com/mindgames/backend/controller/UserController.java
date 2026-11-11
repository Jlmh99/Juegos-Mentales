package com.mindgames.backend.controller;

import java.security.Principal;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindgames.backend.Repositories.UsuarioRepository;
import com.mindgames.backend.config.JwtUtil;
import com.mindgames.backend.entities.Usuario;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public UserController(UsuarioRepository repo, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    // ==========================================================
    // 👤 PERFIL DEL USUARIO AUTENTICADO (cualquier rol, no solo ADMIN)
    // ==========================================================

    @GetMapping("/me")
    public ResponseEntity<?> obtenerPerfil(Principal principal) {
        Usuario user = repo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<?> actualizarPerfil(@RequestBody Usuario datos, Principal principal) {
        Usuario user = repo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (datos.getUsername() != null && !datos.getUsername().isBlank()) {
            user.setUsername(datos.getUsername());
        }

        if (datos.getEmail() != null && !datos.getEmail().isBlank()
                && !datos.getEmail().equalsIgnoreCase(user.getEmail())) {

            if (repo.findByEmail(datos.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Ese correo ya está en uso por otro usuario");
            }
            user.setEmail(datos.getEmail());
        }

        repo.save(user);

        // 🔥 El email pudo cambiar (es el "subject" del token), así que emitimos uno nuevo
        String nuevoToken = jwtUtil.generarToken(user.getEmail(), user.getRol());

        return ResponseEntity.ok(Map.of(
                "usuario", user,
                "token", nuevoToken
        ));
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> body, Principal principal) {
        Usuario user = repo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String actual = body.get("currentPassword");
        String nueva = body.get("newPassword");

        if (actual == null || actual.isBlank() || nueva == null || nueva.isBlank()) {
            return ResponseEntity.badRequest().body("Debes indicar la contraseña actual y la nueva");
        }

        if (!encoder.matches(actual, user.getPassword())) {
            return ResponseEntity.badRequest().body("La contraseña actual es incorrecta");
        }

        user.setPassword(encoder.encode(nueva));
        repo.save(user);

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    // ==========================================================
    // 🖼️ FOTO DE PERFIL DEL USUARIO AUTENTICADO (Base64)
    // Protegido por la regla "/api/users/me/**" -> authenticated() de SecurityConfig.
    // ==========================================================

    // Máximo 2 MB de imagen ya decodificada.
    private static final int MAX_FOTO_BYTES = 2 * 1024 * 1024;
    // Longitud máxima del texto Base64 equivalente (4 caracteres por cada 3 bytes).
    // Se revisa ANTES de decodificar para no gastar memoria con textos enormes.
    private static final int MAX_FOTO_BASE64 = ((MAX_FOTO_BYTES + 2) / 3) * 4;

    @PutMapping("/me/foto")
    public ResponseEntity<?> actualizarFoto(@RequestBody Map<String, String> body, Principal principal) {
        Optional<Usuario> encontrado = repo.findByEmail(principal.getName());
        if (encontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", "Sesión no válida"));
        }

        String foto = body == null ? null : body.get("foto");
        if (foto == null || foto.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Debes enviar una imagen"));
        }
        if (foto.length() > MAX_FOTO_BASE64) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "La imagen no debe superar 2 MB"));
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(foto);
        } catch (IllegalArgumentException e) {
            // No se registra el contenido recibido; solo se responde un mensaje genérico.
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Imagen no válida"));
        }

        if (bytes.length > MAX_FOTO_BYTES) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "La imagen no debe superar 2 MB"));
        }
        if (!esImagenPermitida(bytes)) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Formato de imagen no permitido (usa JPG, PNG o WEBP)"));
        }

        Usuario user = encontrado.get();
        // Se guarda re-codificado para almacenar siempre un Base64 limpio.
        user.setFotoPerfil(Base64.getEncoder().encodeToString(bytes));
        repo.save(user);

        return ResponseEntity.ok(Map.of("mensaje", "Foto actualizada correctamente"));
    }

    @GetMapping("/me/foto")
    public ResponseEntity<?> obtenerFoto(Principal principal) {
        Optional<Usuario> encontrado = repo.findByEmail(principal.getName());
        if (encontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", "Sesión no válida"));
        }
        // Map.of no admite null, por eso singletonMap para responder { "foto": null }.
        return ResponseEntity.ok(Collections.singletonMap("foto", encontrado.get().getFotoPerfil()));
    }

    /** Revisa los primeros bytes ("firma") para aceptar solo JPG, PNG o WEBP. */
    private static boolean esImagenPermitida(byte[] b) {
        boolean jpg = b.length > 3
                && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
        boolean png = b.length > 8
                && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
        boolean webp = b.length > 12
                && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
        return jpg || png || webp;
    }

    @GetMapping
    public List<Usuario> listar() {
        return repo.findAll();
    }

    @PostMapping
    public Usuario crear(@RequestBody Usuario user) {
        user.setPassword(encoder.encode(user.getPassword())); // 🔥 CLAVE
        return repo.save(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, Principal principal) {

        Usuario usuarioActual = repo.findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuarioActual.getId().equals(id)) {
            return ResponseEntity
                .badRequest()
                .body("No puedes eliminar tu propio usuario");
        }

        repo.deleteById(id);
        return ResponseEntity.ok("Usuario eliminado");
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario userActualizado) {

        Usuario user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setUsername(userActualizado.getUsername());
        user.setEmail(userActualizado.getEmail());
        user.setRol(userActualizado.getRol());

        // 🔐 SOLO si viene contraseña NUEVA
        if (userActualizado.getPassword() != null 
            && !userActualizado.getPassword().isEmpty()
            && !userActualizado.getPassword().startsWith("$2a$")) {

            user.setPassword(encoder.encode(userActualizado.getPassword()));
        }

        // 👇 2FA (nuevo campo)
        user.setTwoFactorEnabled(userActualizado.isTwoFactorEnabled());

        return repo.save(user);
    }

    // ==========================================================
    // 🔐 HABILITAR / DESHABILITAR CUENTA (solo ADMIN, vía SecurityConfig)
    // ==========================================================
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, Principal principal) {

        Usuario usuarioActual = repo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuarioActual.getId().equals(id)) {
            return ResponseEntity
                    .badRequest()
                    .body("No puedes deshabilitar tu propia cuenta");
        }

        Usuario user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setHabilitado(!user.isHabilitado());
        repo.save(user);

        return ResponseEntity.ok(user);
    }

}
