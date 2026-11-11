package com.mindgames.backend.dto;

/**
 * Datos que envía la app al terminar una partida.
 * Solo estos tres campos: el id, el usuario y la fecha los decide el servidor,
 * así el cliente no puede guardar partidas a nombre de otro ni cambiar la fecha.
 */
public record PartidaRequest(String juego, Integer tiempoSegundos, Integer puntuacion) {
}
