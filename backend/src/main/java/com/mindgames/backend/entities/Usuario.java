package com.mindgames.backend.entities; // <-- Asegúrate que esto coincida con tus carpetas

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data; // 👈 Asegúrate de tener este import

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
    private String password;
    //private String rol;

    //private boolean twoFactorEnabled;

    // getters y setters
}
