package com.mindgames.backend.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "verificacion_codigos")
public class CodigoVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private LocalDateTime expiracion;
    private boolean usado;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Usuario usuario;

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setExpiracion(LocalDateTime expiracion) {
        this.expiracion = expiracion;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }

    public String getCodigo() { return codigo; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getExpiracion() { return expiracion; }
    public boolean isUsado() { return usado; }
}
