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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Incidencia (ticket de soporte) creada por un usuario autenticado.
 * Se devuelve al cliente siempre a través de IncidenciaResponse (nunca la entidad directa).
 */
@Entity
@Data
@Table(name = "incidencias", indexes = {
        @Index(name = "idx_incidencias_creador", columnList = "creado_por_id"),
        @Index(name = "idx_incidencias_asignado", columnList = "asignado_a_id")
})
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoIncidencia estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadIncidencia prioridad;

    // Siempre es el usuario del token (lo pone IncidenciaService, nunca el cuerpo de la petición).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creado_por_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario creadoPor;

    // Agente que atiende la incidencia (opcional; lo asigna un ADMIN).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_a_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario asignadoA;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    /**
     * Control de concurrencia optimista: JPA incrementa este número en cada
     * actualización. Si dos dispositivos editan la misma versión, el segundo
     * recibe 409 "El registro fue modificado desde otro dispositivo".
     */
    @Version
    private Long version;

    @PrePersist
    void alCrear() {
        LocalDateTime ahora = LocalDateTime.now();
        fechaCreacion = ahora;
        fechaActualizacion = ahora;
    }

    @PreUpdate
    void alActualizar() {
        fechaActualizacion = LocalDateTime.now();
    }
}
