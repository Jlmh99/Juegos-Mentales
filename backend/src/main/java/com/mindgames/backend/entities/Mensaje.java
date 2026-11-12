package com.mindgames.backend.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Mensaje dentro de una incidencia, en el canal PUBLICO o INTERNO.
 * Puede llevar un adjunto (JPG, PNG o PDF de hasta 2 MB) guardado en Base64.
 */
@Entity
@Data
@Table(name = "mensajes_incidencia", indexes = @Index(name = "idx_mensajes_incidencia",
        columnList = "incidencia_id, canal, fecha"))
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incidencia_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Incidencia incidencia;

    // Siempre es el usuario del token.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emisor_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario emisor;

    @Column(nullable = false, length = 1000)
    private String texto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalMensaje canal;

    @Column(nullable = false)
    private LocalDateTime fecha;

    // ---- Adjunto opcional ----
    @Column(length = 255)
    private String adjuntoNombre;

    @Column(length = 50)
    private String adjuntoTipo; // image/jpeg, image/png o application/pdf

    // Base64 del archivo. @ToString.Exclude: nunca volcar el contenido si se imprime el objeto.
    @Column(columnDefinition = "TEXT")
    @ToString.Exclude
    private String adjuntoContenido;

    // Bytes reales del archivo (ya decodificado). Se calcula una sola vez al guardar
    // (MensajeService.aplicarAdjunto) para que el listado del chat NO tenga que leer
    // adjuntoContenido (Práctica 12, Fase 6b). Null en mensajes anteriores a esta
    // columna hasta correr pruebas/backfill_adjunto_tamano.sql.
    // La columna adjunto_tamano (BIGINT, nullable) la crea Hibernate (ddl-auto=update).
    private Long adjuntoTamano;

    @PrePersist
    void alCrear() {
        fecha = LocalDateTime.now();
    }
}
