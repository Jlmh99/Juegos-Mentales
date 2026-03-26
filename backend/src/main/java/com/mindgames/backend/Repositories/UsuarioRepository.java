package com.mindgames.backend.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mindgames.backend.entities.Usuario; // 👈 Importante para manejar si el usuario no existe

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    /**
     * Busca un usuario por su correo electrónico.
     * Retorna un Optional para evitar errores de NullPointerException
     * si el correo no está en la base de datos.
     */
    Optional<Usuario> findByEmail(String email);
}
