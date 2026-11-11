package com.mindgames.backend.config;

/**
 * Roles de la aplicación. Se guardan como texto en la columna usuarios.rol
 * (así no hay que migrar la columna ni cambiar el frontend web).
 *
 *  - USER:   usuario normal; crea y consulta sus propias incidencias.
 *  - AGENTE: atiende las incidencias que un ADMIN le asigna.
 *  - ADMIN:  ve y administra todo.
 */
public final class Roles {

    public static final String ADMIN = "ADMIN";
    public static final String AGENTE = "AGENTE";
    public static final String USER = "USER";

    private Roles() {
    }
}
