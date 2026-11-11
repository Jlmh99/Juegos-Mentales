package com.mindgames.backend.dto;

import java.time.LocalDateTime;

import com.mindgames.backend.entities.Partida;

/**
 * Lo que se devuelve al cliente de una partida (sin datos del usuario).
 */
public record PartidaResponse(Long id, String juego, Integer tiempoSegundos,
                              Integer puntuacion, LocalDateTime fecha) {

    public static PartidaResponse de(Partida p) {
        return new PartidaResponse(p.getId(), p.getJuego(), p.getTiempoSegundos(),
                p.getPuntuacion(), p.getFecha());
    }
}
