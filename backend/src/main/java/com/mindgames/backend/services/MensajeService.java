package com.mindgames.backend.services;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mindgames.backend.Repositories.MensajeRepository;
import com.mindgames.backend.dto.AdjuntoDto;
import com.mindgames.backend.dto.MensajeRequest;
import com.mindgames.backend.dto.MensajeResponse;
import com.mindgames.backend.entities.CanalMensaje;
import com.mindgames.backend.entities.Incidencia;
import com.mindgames.backend.entities.Mensaje;
import com.mindgames.backend.entities.Usuario;

/**
 * Mensajes de una incidencia en dos canales:
 *  - PUBLICO: creador, agente asignado y ADMIN.
 *  - INTERNO: solo agente asignado y ADMIN (el creador recibe 403).
 * Valida texto (obligatorio, máx. 1000) y adjuntos (JPG/PNG/PDF, máx. 2 MB).
 */
@Service
public class MensajeService {

    static final int MAX_TEXTO = 1000;
    static final int MAX_ADJUNTO_BYTES = 2 * 1024 * 1024;
    // Longitud máxima del Base64 equivalente: se revisa ANTES de decodificar.
    static final int MAX_ADJUNTO_BASE64 = ((MAX_ADJUNTO_BYTES + 2) / 3) * 4;

    /** Tipos permitidos (se acepta "image/jpg" como alias de "image/jpeg"). */
    private static final Map<String, String> TIPOS = Map.of(
            "image/jpeg", "image/jpeg",
            "image/jpg", "image/jpeg",
            "image/png", "image/png",
            "application/pdf", "application/pdf");

    private final MensajeRepository repo;
    private final IncidenciaService incidencias;

    public MensajeService(MensajeRepository repo, IncidenciaService incidencias) {
        this.repo = repo;
        this.incidencias = incidencias;
    }

    @Transactional(readOnly = true)
    public List<MensajeResponse> listar(Long incidenciaId, CanalMensaje canal, String email) {
        Usuario yo = incidencias.usuarioAutenticado(email);
        Incidencia inc = incidencias.buscar(incidenciaId);
        CanalMensaje c = canal == null ? CanalMensaje.PUBLICO : canal;
        verificarAcceso(inc, yo, c);
        // Sin el Base64 de los adjuntos: se descargan aparte con adjunto() (Práctica 12).
        return repo.listarSinContenido(inc, c).stream()
                .map(MensajeResponse::de).toList();
    }

    /** Archivo adjunto ya decodificado, listo para enviarse como binario. */
    public record ArchivoAdjunto(String nombre, String tipo, byte[] bytes) {
    }

    /**
     * GET /api/incidencias/{id}/mensajes/{mid}/adjunto con los mismos permisos que el listado:
     *  1. la incidencia debe existir (404) y el usuario debe poder verla (403);
     *  2. el mensaje debe pertenecer a ESA incidencia (si no, 404: evita pedir el {mid} de otra);
     *  3. se valida el canal REAL guardado en el mensaje, nunca uno que mande el cliente
     *     (un USER no puede bajar adjuntos del canal INTERNO: 403);
     *  4. el mensaje debe tener adjunto (404).
     */
    @Transactional(readOnly = true)
    public ArchivoAdjunto adjunto(Long incidenciaId, Long mensajeId, String email) {
        Usuario yo = incidencias.usuarioAutenticado(email);
        Incidencia inc = incidencias.buscar(incidenciaId);
        if (!incidencias.puedeVer(inc, yo)) throw ErrorApi.prohibido();

        Mensaje m = repo.findById(mensajeId)
                .filter(x -> x.getIncidencia().getId().equals(inc.getId()))
                .orElseThrow(ErrorApi::adjuntoNoEncontrado);
        verificarAcceso(inc, yo, m.getCanal());
        if (m.getAdjuntoContenido() == null) throw ErrorApi.adjuntoNoEncontrado();

        return new ArchivoAdjunto(m.getAdjuntoNombre(), m.getAdjuntoTipo(),
                Base64.getDecoder().decode(m.getAdjuntoContenido()));
    }

    @Transactional
    public MensajeResponse enviar(Long incidenciaId, MensajeRequest datos, String email) {
        Usuario yo = incidencias.usuarioAutenticado(email);
        Incidencia inc = incidencias.buscar(incidenciaId);
        if (datos == null) throw ErrorApi.datosInvalidos("Datos no válidos");
        CanalMensaje canal = datos.canal() == null ? CanalMensaje.PUBLICO : datos.canal();
        verificarAcceso(inc, yo, canal);

        Mensaje m = new Mensaje();
        m.setIncidencia(inc);
        m.setEmisor(yo); // SIEMPRE el usuario del token
        m.setCanal(canal);
        m.setTexto(validarTexto(datos.texto()));
        if (datos.adjunto() != null) aplicarAdjunto(m, datos.adjunto());
        return MensajeResponse.de(repo.save(m));
    }

    // ---------------------------------------------------------------------

    /** PUBLICO: quien puede ver la incidencia. INTERNO: solo ADMIN o agente asignado. */
    private void verificarAcceso(Incidencia inc, Usuario yo, CanalMensaje canal) {
        if (!incidencias.puedeVer(inc, yo)) throw ErrorApi.prohibido();
        if (canal == CanalMensaje.INTERNO
                && !incidencias.esAdmin(yo) && !incidencias.esAgenteAsignado(inc, yo)) {
            throw ErrorApi.prohibido();
        }
    }

    private static String validarTexto(String texto) {
        String t = texto == null ? "" : texto.trim();
        if (t.isEmpty()) throw ErrorApi.datosInvalidos("El mensaje no puede estar vacío");
        if (t.length() > MAX_TEXTO) {
            throw ErrorApi.datosInvalidos("El mensaje no puede superar " + MAX_TEXTO + " caracteres");
        }
        return t;
    }

    /**
     * Valida y guarda el adjunto:
     *  - tipo declarado permitido (JPG, PNG, PDF),
     *  - tamaño revisado antes y después de decodificar (máx. 2 MB),
     *  - los primeros bytes del archivo deben coincidir con el tipo declarado
     *    (evita, por ejemplo, subir un ejecutable renombrado como .png).
     */
    private static void aplicarAdjunto(Mensaje m, AdjuntoDto adjunto) {
        String tipo = adjunto.tipo() == null ? null : TIPOS.get(adjunto.tipo().trim().toLowerCase());
        if (tipo == null) throw ErrorApi.datosInvalidos("Tipo de archivo no permitido (usa JPG, PNG o PDF)");

        String contenido = adjunto.contenido();
        if (contenido == null || contenido.isBlank()) throw ErrorApi.datosInvalidos("El adjunto está vacío");
        if (contenido.length() > MAX_ADJUNTO_BASE64) {
            throw ErrorApi.datosInvalidos("El adjunto no debe superar 2 MB");
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(contenido.trim());
        } catch (IllegalArgumentException e) {
            throw ErrorApi.datosInvalidos("Adjunto no válido"); // no se registra el contenido
        }
        if (bytes.length == 0) throw ErrorApi.datosInvalidos("El adjunto está vacío");
        if (bytes.length > MAX_ADJUNTO_BYTES) throw ErrorApi.datosInvalidos("El adjunto no debe superar 2 MB");
        if (!coincideFirma(bytes, tipo)) {
            throw ErrorApi.datosInvalidos("El contenido del adjunto no coincide con su tipo");
        }

        m.setAdjuntoNombre(limpiarNombre(adjunto.nombre()));
        m.setAdjuntoTipo(tipo);
        m.setAdjuntoContenido(Base64.getEncoder().encodeToString(bytes)); // Base64 limpio
    }

    /** Revisa los primeros bytes ("firma") del archivo. */
    private static boolean coincideFirma(byte[] b, String tipo) {
        return switch (tipo) {
            case "image/jpeg" -> b.length > 3
                    && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
            case "image/png" -> b.length > 8
                    && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
            case "application/pdf" -> b.length > 4
                    && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F';
            default -> false;
        };
    }

    /** Solo el nombre del archivo (sin rutas ni caracteres de control), máx. 255. */
    private static String limpiarNombre(String nombre) {
        String n = nombre == null ? "" : nombre.trim();
        n = n.substring(Math.max(n.lastIndexOf('/'), n.lastIndexOf('\\')) + 1);
        n = n.replaceAll("[\\p{Cntrl}]", "");
        if (n.isEmpty()) n = "adjunto";
        return n.length() > 255 ? n.substring(0, 255) : n;
    }
}
