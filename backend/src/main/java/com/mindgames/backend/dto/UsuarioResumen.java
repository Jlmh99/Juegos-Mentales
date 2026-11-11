package com.mindgames.backend.dto;

import com.mindgames.backend.entities.Usuario;

/** Datos públicos mínimos de un usuario (sin contraseña, foto ni estado de 2FA). */
public record UsuarioResumen(Long id, String username, String email, String rol) {

    public static UsuarioResumen de(Usuario u) {
        return u == null ? null : new UsuarioResumen(u.getId(), u.getUsername(), u.getEmail(), u.getRol());
    }
}
