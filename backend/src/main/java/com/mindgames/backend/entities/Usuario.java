package com.mindgames.backend.entities; // <-- Asegúrate que esto coincida con tus carpetas

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data; // 👈 Asegúrate de tener este import
import lombok.ToString;

@Entity
@Data // 👈 Esta anotación genera automáticamente getEmail(), setEmail(), etc.
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    @Column(unique = true)
    private String email;

    // 🔐 Se puede recibir en el JSON de entrada, pero nunca se serializa de vuelta al cliente
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String rol;
    private boolean twoFactorEnabled;

    // 🔐 Estado de la cuenta: true = habilitada (puede iniciar sesión), false = deshabilitada
    @Column(columnDefinition = "boolean default true")
    private boolean habilitado = true;

    // 🖼️ Foto de perfil en Base64 (hasta 2 MB ya decodificada, validado en UserController).
    // @JsonIgnore: no viaja en /api/users ni /api/users/me (sería muy pesada);
    // solo se lee y escribe con /api/users/me/foto.
    // @ToString.Exclude: evita volcar el Base64 completo si alguien imprime el usuario.
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    @ToString.Exclude
    private String fotoPerfil;

    // getters y setters
}
