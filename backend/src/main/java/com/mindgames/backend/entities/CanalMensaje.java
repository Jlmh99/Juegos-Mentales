package com.mindgames.backend.entities;

/**
 * Canal de un mensaje dentro de una incidencia.
 *  - PUBLICO: lo ven el creador, el agente asignado y los ADMIN.
 *  - INTERNO: solo el agente asignado y los ADMIN (chat admin-agente);
 *             el creador (USER) no puede leerlo ni escribir en él.
 */
public enum CanalMensaje {
    PUBLICO,
    INTERNO
}
