-- =====================================================================
-- Roles de prueba para incidencias (ADMIN, AGENTE, USER)
-- Ejecutar en la base JuegosMentales_BD, por ejemplo:
--   psql -U postgres -d JuegosMentales_BD -f sql/roles_prueba.sql
-- Cambia los correos por cuentas que ya existan (regístralas antes en la app/web).
-- =====================================================================

-- Promover a ADMIN
UPDATE usuarios SET rol = 'ADMIN'  WHERE email = 'admin@prueba.com';

-- Promover a AGENTE
UPDATE usuarios SET rol = 'AGENTE' WHERE email = 'agente@prueba.com';

-- Regresar a USER (por si una cuenta de prueba quedó con otro rol)
UPDATE usuarios SET rol = 'USER'   WHERE email IN ('usuario.a@prueba.com', 'usuario.b@prueba.com');

-- Verificar
SELECT id, username, email, rol, habilitado FROM usuarios ORDER BY rol, email;

-- NOTA:
--  * Incidencias y mensajes usan el rol guardado en la BD: el cambio aplica de inmediato.
--  * El rol dentro del JWT (y las rutas /api/users que exigen ADMIN) se actualiza
--    hasta que la cuenta vuelva a iniciar sesión.
