package com.mindgames.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder encoder;
    @PostMapping("/register")
    public String register(@RequestBody Usuario user) {

        // validar si ya existe
        if (repo.findByEmail(user.getEmail()).isPresent()) {
            return "El usuario ya existe";
        }
        // 🔐 encriptar contraseña
        user.setPassword(encoder.encode(user.getPassword()));

        repo.save(user);
        return "Usuario registrado correctamente";
    }

    @PostMapping("/login")
    public String login(@RequestBody Usuario login) {

        Usuario user = repo.findByEmail(login.getEmail()).orElse(null);

        if (user == null) {
            return "Usuario no encontrado";
        }
        // 🔐 comparar contraseña encriptada
        if (!encoder.matches(login.getPassword(), user.getPassword())) {
            return "Contraseña incorrecta";
        }
        return "Login correcto";
    }
}
