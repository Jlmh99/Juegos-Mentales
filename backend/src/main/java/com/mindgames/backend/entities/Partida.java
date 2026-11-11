package com.mindgames.backend.entities;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Partida terminada de un minijuego (Sudoku o Crucigrama).
 * Cada partida pertenece a un usuario; la fecha la pone el servidor.
 */
@Entity
@Data
@Table(name = "partidas",
       indexes = @Index(name = "idx_partidas_usuario_fecha", columnList = "usuario_id, fecha"))
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dueño de la partida. LAZY: no se carga el usuario completo si no se necesita.
    // Se excluye de JSON/toString/equals para no exponer datos del usuario
    // ni provocar cargas innecesarias.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    // "SUDOKU" o "CRUCIGRAMA" (validado en PartidaController)
    @Column(nullable = false, length = 20)
    private String juego;

    @Column(nullable = false)
    private Integer tiempoSegundos;

    @Column(nullable = false)
    private Integer puntuacion;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
