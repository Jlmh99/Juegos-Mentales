package com.mindgames.backend.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mindgames.backend.Repositories.MensajeRepository;
import com.mindgames.backend.dto.AdjuntoDto;
import com.mindgames.backend.dto.MensajeFila;
import com.mindgames.backend.dto.MensajeRequest;
import com.mindgames.backend.dto.MensajeResponse;
import com.mindgames.backend.entities.CanalMensaje;
import com.mindgames.backend.entities.Incidencia;
import com.mindgames.backend.entities.Mensaje;
import com.mindgames.backend.entities.Usuario;

/**
 * Práctica 12, Fase 6b: el tamaño del adjunto se guarda una sola vez al enviar
 * y el listado lo toma de adjunto_tamano, sin leer adjunto_contenido.
 * Pruebas unitarias (sin base de datos): repositorio e IncidenciaService simulados.
 */
class MensajeTamanoAdjuntoTest {

    // PNG de 1x1 (70 bytes), el mismo de pruebas/probar_incidencias.sh.
    private static final String PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";

    private final MensajeRepository repo = mock(MensajeRepository.class);
    private final IncidenciaService incidencias = mock(IncidenciaService.class);
    private final MensajeService servicio = new MensajeService(repo, incidencias);

    private final Usuario yo = new Usuario();
    private final Incidencia inc = new Incidencia();

    MensajeTamanoAdjuntoTest() {
        yo.setId(1L);
        yo.setEmail("u@x.com");
        yo.setRol("USER");
        inc.setId(7L);
        when(incidencias.usuarioAutenticado("u@x.com")).thenReturn(yo);
        when(incidencias.buscar(7L)).thenReturn(inc);
        when(incidencias.puedeVer(inc, yo)).thenReturn(true);
        when(repo.save(any(Mensaje.class))).thenAnswer(a -> a.getArgument(0));
    }

    @Test
    void alEnviarGuardaElTamanoRealDeLosBytesDecodificados() {
        MensajeResponse r = servicio.enviar(7L,
                new MensajeRequest("Captura", null, new AdjuntoDto("c.png", "image/png", PNG)), "u@x.com");

        assertThat(r.tieneAdjunto()).isTrue();
        assertThat(r.adjunto().tamano()).isEqualTo(70L)
                .isEqualTo(Base64.getDecoder().decode(PNG).length);
    }

    @Test
    void mensajeSinAdjuntoNoTieneTamano() {
        MensajeResponse r = servicio.enviar(7L, new MensajeRequest("Hola", null, null), "u@x.com");

        assertThat(r.tieneAdjunto()).isFalse();
        assertThat(r.adjunto()).isNull();
    }

    @Test
    void elListadoUsaAdjuntoTamanoYNoLeeElContenido() {
        when(repo.listarSinContenido(inc, CanalMensaje.PUBLICO)).thenReturn(List.of(
                fila(1L, "c.png", "image/png", 70L),
                fila(2L, null, null, null)));

        List<MensajeResponse> lista = servicio.listar(7L, CanalMensaje.PUBLICO, "u@x.com");

        assertThat(lista.get(0).tieneAdjunto()).isTrue();
        assertThat(lista.get(0).adjunto().tamano()).isEqualTo(70L);
        assertThat(lista.get(1).tieneAdjunto()).isFalse();
        assertThat(lista.get(1).adjunto()).isNull();
        // Nunca se cargan entidades completas (con adjunto_contenido) para listar.
        verify(repo, never()).findAll();
        verify(repo, never()).findById(any());
    }

    @Test
    void mensajeAntiguoSinBackfillDevuelveTamanoCero() {
        MensajeResponse r = MensajeResponse.de(fila(3L, "viejo.jpg", "image/jpeg", null));

        assertThat(r.tieneAdjunto()).isTrue();
        assertThat(r.adjunto().tamano()).isZero();
    }

    private static MensajeFila fila(Long id, String nombre, String tipo, Long tamano) {
        return new MensajeFila(id, 7L, 1L, "u", "u@x.com", "USER", "texto",
                CanalMensaje.PUBLICO, LocalDateTime.of(2026, 11, 12, 9, 0), nombre, tipo, tamano);
    }
}
