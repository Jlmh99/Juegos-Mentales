package com.mindgames.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindgames.backend.Repositories.CodigoRepository;
import com.mindgames.backend.Repositories.UsuarioRepository;
import com.mindgames.backend.entities.CodigoVerificacion;
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

        // 👇 TODOS los registros normales son USER
        user.setRol("USER");

        repo.save(user);
        return "Usuario registrado correctamente";
    }

    @Autowired
    private CodigoRepository codigoRepo;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Usuario login) {

        Usuario user = repo.findByEmail(login.getEmail()).orElse(null);

        if (user == null) return Map.of("mensaje", "Usuario no encontrado");

        if (!encoder.matches(login.getPassword(), user.getPassword()))
        return Map.of("mensaje", "Contraseña incorrecta");

        // 🔐 SI TIENE 2FA (Bloque integrado exactamente como lo pediste)
    if (user.isTwoFactorEnabled()) {
        String codigo = String.valueOf((int)(Math.random()*900000)+100000);

        CodigoVerificacion c = new CodigoVerificacion();
        c.setCodigo(codigo);
        c.setUsuario(user);
        c.setExpiracion(LocalDateTime.now().plusMinutes(5));
        c.setUsado(false);

        codigoRepo.save(c);

        System.out.println("Código 2FA: " + codigo); // simulación

        return Map.of("mensaje", "2FA requerido");
        }
        
        return Map.of(
        "mensaje", "Login correcto",
        "rol", user.getRol(),
        "email", user.getEmail()
        );
    }

    @PostMapping("/verify")
    public Map<String, String> verify(@RequestBody Map<String, String> data) {
        String email = data.get("email");
        String codigo = data.get("codigo");

        Usuario user = repo.findByEmail(email).orElse(null);

        if (user == null) return Map.of("mensaje", "Usuario no encontrado");

        List<CodigoVerificacion> codigos = codigoRepo.findAll();

        for (CodigoVerificacion c : codigos) {
            if (c.getUsuario().getId().equals(user.getId())
                && c.getCodigo().equals(codigo)
                && !c.isUsado()
                && c.getExpiracion().isAfter(LocalDateTime.now())) {

                c.setUsado(true);
                codigoRepo.save(c);

                // ✅ Devolvemos un MAPA con el ROL incluido para el Guard
                return Map.of(
                    "mensaje", "Login correcto",
                    "rol", user.getRol(),
                    "email", user.getEmail()
                );
            }
        }

        return Map.of("mensaje", "Código inválido");
    }
}
