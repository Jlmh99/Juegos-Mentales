package com.mindgames.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindgames.backend.Repositories.UsuarioRepository;
import com.mindgames.backend.entities.Usuario;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository repo;

    public AuthController(UsuarioRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/register")
    public String register(@RequestBody Usuario user) {

        // validar si ya existe
        if (repo.findByEmail(user.getEmail()).isPresent()) {
            return "El usuario ya existe";
        }

        repo.save(user);
        return "Usuario registrado correctamente";
    }
}
