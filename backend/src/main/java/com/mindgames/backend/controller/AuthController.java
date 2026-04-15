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
import com.mindgames.backend.config.JwtUtil;
import com.mindgames.backend.entities.CodigoVerificacion;
import com.mindgames.backend.entities.Usuario;
import com.mindgames.backend.services.EmailService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository repo;

    public AuthController(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private CodigoRepository codigoRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService; // 👈 Inyectamos el servicio

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

        // 📧 ENVÍO REAL CON BREVO
            try {
                emailService.enviarCodigo(user.getEmail(), codigo);
                System.out.println("✅ Correo enviado a: " + user.getEmail());
            } catch (Exception e) {
                System.err.println("❌ Error enviando correo: " + e.getMessage());
                return Map.of("mensaje", "Error al enviar el código de verificación");
            }

            return Map.of("mensaje", "2FA requerido");
        }

        // 🔥 2. Generar Token para usuarios SIN 2FA
        String token = jwtUtil.generarToken(user.getEmail(), user.getRol());
        
        return Map.of(
        "mensaje", "Login correcto",
        "token", token,
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

                // 🔥 3. Generar Token para usuarios CON 2FA (tras verificar código)
                String token = jwtUtil.generarToken(user.getEmail(), user.getRol());

                // ✅ Devolvemos un MAPA con el ROL incluido para el Guard
                return Map.of(
                    "mensaje", "Login correcto",
                    "token", token,
                    "rol", user.getRol(),
                    "email", user.getEmail()
                );
            }
        }

        return Map.of("mensaje", "Código inválido");
    }
}
