package com.mindgames.backend.services;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mindgames.backend.Repositories.IncidenciaRepository;
import com.mindgames.backend.Repositories.MensajeRepository;
import com.mindgames.backend.Repositories.UsuarioRepository;
import com.mindgames.backend.config.Roles;
import com.mindgames.backend.dto.AsignarRequest;
import com.mindgames.backend.dto.IncidenciaActualizarRequest;
import com.mindgames.backend.dto.IncidenciaCrearRequest;
import com.mindgames.backend.dto.IncidenciaResponse;
import com.mindgames.backend.dto.UsuarioResumen;
import com.mindgames.backend.entities.EstadoIncidencia;
import com.mindgames.backend.entities.Incidencia;
import com.mindgames.backend.entities.PrioridadIncidencia;
import com.mindgames.backend.entities.Usuario;

/**
 * Reglas de negocio y AUTORIZACIÓN de las incidencias.
 *
 * SEGURIDAD:
 *  - Los permisos se deciden aquí, en el backend (ocultar botones en la app no basta).
 *  - El usuario siempre sale del token (email del Principal), nunca del cuerpo.
 *  - Se usa el rol GUARDADO EN LA BASE DE DATOS (no el del token): si un ADMIN
 *    promueve o degrada a alguien, el cambio aplica de inmediato.
 *  - Los mensajes de error son genéricos y no se registra nada sensible en logs.
 */
@Service
public class IncidenciaService {

    static final int MAX_TITULO = 150;
    static final int MAX_DESCRIPCION = 2000;

    private final IncidenciaRepository repo;
    private final UsuarioRepository usuarios;
    private final MensajeRepository mensajes;

    public IncidenciaService(IncidenciaRepository repo, UsuarioRepository usuarios,
                             MensajeRepository mensajes) {
        this.repo = repo;
        this.usuarios = usuarios;
        this.mensajes = mensajes;
    }

    // =====================================================================
    // Consultas
    // =====================================================================

    /** USER: solo las suyas. AGENTE: solo las asignadas a él. ADMIN: todas. */
    @Transactional(readOnly = true)
    public List<IncidenciaResponse> listar(String email) {
        Usuario yo = usuarioAutenticado(email);
        List<Incidencia> lista;
        if (esAdmin(yo)) {
            lista = repo.findAllByOrderByFechaCreacionDesc();
        } else if (esAgente(yo)) {
            lista = repo.findByAsignadoAOrderByFechaCreacionDesc(yo);
        } else {
            lista = repo.findByCreadoPorOrderByFechaCreacionDesc(yo);
        }
        return lista.stream().map(IncidenciaResponse::de).toList();
    }

    /** Solo el creador, el agente asignado o un ADMIN. */
    @Transactional(readOnly = true)
    public IncidenciaResponse obtener(Long id, String email) {
        Usuario yo = usuarioAutenticado(email);
        Incidencia inc = buscar(id);
        if (!puedeVer(inc, yo)) throw ErrorApi.prohibido();
        return IncidenciaResponse.de(inc);
    }

    /** Solo ADMIN: agentes habilitados que se pueden asignar. */
    @Transactional(readOnly = true)
    public List<UsuarioResumen> agentes(String email) {
        Usuario yo = usuarioAutenticado(email);
        if (!esAdmin(yo)) throw ErrorApi.prohibido();
        return usuarios.findByRolAndHabilitadoTrue(Roles.AGENTE).stream()
                .map(UsuarioResumen::de).toList();
    }

    // =====================================================================
    // Crear
    // =====================================================================

    @Transactional
    public IncidenciaResponse crear(IncidenciaCrearRequest datos, String email) {
        Usuario yo = usuarioAutenticado(email);
        if (datos == null) throw ErrorApi.datosInvalidos("Datos no válidos");

        // SEGURIDAD: no se permite crear a nombre de otro usuario.
        if (esOtroUsuario(datos.creadoPor(), yo)) throw ErrorApi.prohibido();

        Incidencia inc = new Incidencia();
        inc.setTitulo(validarTitulo(datos.titulo()));
        inc.setDescripcion(validarDescripcion(datos.descripcion()));
        inc.setEstado(EstadoIncidencia.PENDIENTE); // toda incidencia nace pendiente
        inc.setPrioridad(datos.prioridad() != null ? datos.prioridad() : PrioridadIncidencia.MEDIA);
        inc.setCreadoPor(yo); // SIEMPRE el usuario del token
        return IncidenciaResponse.de(repo.saveAndFlush(inc));
    }

    // =====================================================================
    // Actualizar
    // =====================================================================

    /**
     * - Creador: título y descripción.
     * - Agente asignado: estado.
     * - ADMIN: todo, incluida la prioridad.
     * Intentar cambiar algo que tu rol no permite -> 403.
     * Versión distinta a la guardada -> 409.
     */
    @Transactional
    public IncidenciaResponse actualizar(Long id, IncidenciaActualizarRequest datos, String email) {
        Usuario yo = usuarioAutenticado(email);
        Incidencia inc = buscar(id);
        if (!puedeVer(inc, yo)) throw ErrorApi.prohibido();
        if (datos == null) throw ErrorApi.datosInvalidos("Datos no válidos");

        // El creador de una incidencia nunca se puede cambiar.
        if (esOtroUsuario(datos.creadoPor(), inc.getCreadoPor())) throw ErrorApi.prohibido();

        // ¿Qué quiere cambiar? (mandar el mismo valor que ya tiene no cuenta como cambio)
        String nuevoTitulo = datos.titulo() == null ? null : datos.titulo().trim();
        String nuevaDescripcion = datos.descripcion() == null ? null : datos.descripcion().trim();
        boolean cambiaTitulo = nuevoTitulo != null && !nuevoTitulo.equals(inc.getTitulo());
        boolean cambiaDescripcion = nuevaDescripcion != null && !nuevaDescripcion.equals(inc.getDescripcion());
        boolean cambiaEstado = datos.estado() != null && datos.estado() != inc.getEstado();
        boolean cambiaPrioridad = datos.prioridad() != null && datos.prioridad() != inc.getPrioridad();

        // SEGURIDAD: permisos por campo según el rol en esta incidencia.
        if (!esAdmin(yo)) {
            boolean creador = esCreador(inc, yo);
            boolean agenteAsignado = esAgenteAsignado(inc, yo);
            if ((cambiaTitulo || cambiaDescripcion) && !creador) throw ErrorApi.prohibido();
            if (cambiaEstado && !agenteAsignado) throw ErrorApi.prohibido();
            if (cambiaPrioridad) throw ErrorApi.prohibido(); // solo ADMIN
        }

        // Concurrencia optimista: la versión enviada debe ser la guardada.
        if (datos.version() == null) throw ErrorApi.datosInvalidos("Falta la versión del registro");
        if (!datos.version().equals(inc.getVersion())) throw ErrorApi.conflictoVersion();

        if (cambiaTitulo) inc.setTitulo(validarTitulo(nuevoTitulo));
        if (cambiaDescripcion) inc.setDescripcion(validarDescripcion(nuevaDescripcion));
        if (cambiaEstado) inc.setEstado(datos.estado());
        if (cambiaPrioridad) inc.setPrioridad(datos.prioridad());

        boolean huboCambios = cambiaTitulo || cambiaDescripcion || cambiaEstado || cambiaPrioridad;
        // saveAndFlush: si otro dispositivo guardó justo en medio, JPA lanza
        // OptimisticLockingFailureException y el manejador responde 409.
        return IncidenciaResponse.de(huboCambios ? repo.saveAndFlush(inc) : inc);
    }

    /** Solo ADMIN: asigna la incidencia a un usuario con rol AGENTE. */
    @Transactional
    public IncidenciaResponse asignar(Long id, AsignarRequest datos, String email) {
        Usuario yo = usuarioAutenticado(email);
        if (!esAdmin(yo)) throw ErrorApi.prohibido();
        Incidencia inc = buscar(id);

        if (datos == null || datos.agenteId() == null) {
            throw ErrorApi.datosInvalidos("Debes indicar el agente");
        }
        Usuario agente = usuarios.findById(datos.agenteId())
                .filter(u -> esAgente(u) && u.isHabilitado())
                .orElseThrow(() -> ErrorApi.datosInvalidos("El usuario indicado no es un agente válido"));

        inc.setAsignadoA(agente);
        return IncidenciaResponse.de(repo.saveAndFlush(inc));
    }

    // =====================================================================
    // Eliminar
    // =====================================================================

    /** Solo el creador o un ADMIN (el agente asignado no puede borrar). */
    @Transactional
    public void eliminar(Long id, String email) {
        Usuario yo = usuarioAutenticado(email);
        Incidencia inc = buscar(id);
        if (!esAdmin(yo) && !esCreador(inc, yo)) throw ErrorApi.prohibido();
        mensajes.deleteByIncidencia(inc); // primero sus mensajes
        repo.delete(inc);
    }

    // =====================================================================
    // Reglas reutilizadas también por MensajeService
    // =====================================================================

    /** Usuario del token; si ya no existe o está deshabilitado, la sesión no es válida. */
    Usuario usuarioAutenticado(String email) {
        return usuarios.findByEmail(email)
                .filter(Usuario::isHabilitado)
                .orElseThrow(() -> new ErrorApi(HttpStatus.UNAUTHORIZED, "Sesión no válida"));
    }

    Incidencia buscar(Long id) {
        return repo.findById(id).orElseThrow(ErrorApi::noEncontrada);
    }

    /** Puede ver la incidencia: el creador, el agente asignado o un ADMIN. */
    boolean puedeVer(Incidencia inc, Usuario yo) {
        return esAdmin(yo) || esCreador(inc, yo) || esAgenteAsignado(inc, yo);
    }

    boolean esAdmin(Usuario u) {
        return Roles.ADMIN.equals(u.getRol());
    }

    boolean esAgente(Usuario u) {
        return Roles.AGENTE.equals(u.getRol());
    }

    boolean esCreador(Incidencia inc, Usuario u) {
        return mismoUsuario(inc.getCreadoPor(), u);
    }

    /** Es el agente asignado Y sigue teniendo rol AGENTE (si lo degradan, pierde acceso). */
    boolean esAgenteAsignado(Incidencia inc, Usuario u) {
        return esAgente(u) && mismoUsuario(inc.getAsignadoA(), u);
    }

    // =====================================================================
    // Auxiliares privados
    // =====================================================================

    private static boolean mismoUsuario(Usuario a, Usuario b) {
        return a != null && b != null && Objects.equals(a.getId(), b.getId());
    }

    /**
     * true si el "creadoPor" del cuerpo apunta a alguien distinto de [usuario].
     * Acepta email, id o un objeto {"id": .., "email": ..}. Si no viene, es false.
     */
    private static boolean esOtroUsuario(Object creadoPor, Usuario usuario) {
        if (creadoPor == null) return false;
        if (creadoPor instanceof Number n) {
            return !Objects.equals(n.longValue(), usuario.getId());
        }
        if (creadoPor instanceof String s) {
            String t = s.trim();
            if (t.isEmpty()) return false;
            return !t.equalsIgnoreCase(usuario.getEmail()) && !t.equals(String.valueOf(usuario.getId()));
        }
        if (creadoPor instanceof Map<?, ?> m) {
            Object id = m.get("id");
            Object email = m.get("email");
            return (id != null && esOtroUsuario(id, usuario)) || (email != null && esOtroUsuario(email, usuario));
        }
        return true; // formato desconocido: se trata como ajeno (se rechaza)
    }

    private static String validarTitulo(String titulo) {
        String t = titulo == null ? "" : titulo.trim();
        if (t.isEmpty()) throw ErrorApi.datosInvalidos("El título es obligatorio");
        if (t.length() > MAX_TITULO) throw ErrorApi.datosInvalidos("El título no puede superar " + MAX_TITULO + " caracteres");
        return t;
    }

    private static String validarDescripcion(String descripcion) {
        String d = descripcion == null ? "" : descripcion.trim();
        if (d.isEmpty()) throw ErrorApi.datosInvalidos("La descripción es obligatoria");
        if (d.length() > MAX_DESCRIPCION) throw ErrorApi.datosInvalidos("La descripción no puede superar " + MAX_DESCRIPCION + " caracteres");
        return d;
    }
}
