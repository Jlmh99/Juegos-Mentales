package com.mindgames.backend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindgames.backend.Repositories.UsuarioRepository;
import com.mindgames.backend.entities.Usuario;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UserController(UsuarioRepository repo,PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
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

}
