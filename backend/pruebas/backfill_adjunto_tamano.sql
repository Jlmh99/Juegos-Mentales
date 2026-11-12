-- =====================================================================
-- Práctica 12, Fase 6b: backfill de mensajes_incidencia.adjunto_tamano
--
-- Llena el tamaño real (bytes) de los adjuntos que se guardaron ANTES de que
-- existiera la columna adjunto_tamano. Los mensajes nuevos ya lo guardan al
-- enviarse (MensajeService.aplicarAdjunto).
--
-- Cuándo correrlo: DESPUÉS de desplegar el backend con la Fase 6b, porque
-- Hibernate (ddl-auto=update) crea la columna en el primer arranque.
-- Se puede correr más de una vez: solo toca filas con adjunto_tamano NULL.
--
-- Cálculo: adjunto_contenido es Base64 "limpio" (sin saltos de línea; lo
-- garantiza aplicarAdjunto, que lo re-codifica). Cada 4 caracteres son 3 bytes,
-- menos 1 byte por cada '=' de relleno al final (0, 1 o 2).
--
-- Uso:  psql "<cadena de conexión>" -f pruebas/backfill_adjunto_tamano.sql
-- (no contiene credenciales; la conexión la pone quien lo ejecuta)
-- =====================================================================

BEGIN;

-- Antes: cuántos adjuntos no tienen tamaño todavía.
SELECT count(*) AS adjuntos_sin_tamano
FROM mensajes_incidencia
WHERE adjunto_contenido IS NOT NULL AND adjunto_tamano IS NULL;

UPDATE mensajes_incidencia
SET adjunto_tamano = (length(adjunto_contenido) / 4) * 3
                   - CASE
                       WHEN right(adjunto_contenido, 2) = '==' THEN 2
                       WHEN right(adjunto_contenido, 1) = '='  THEN 1
                       ELSE 0
                     END
WHERE adjunto_contenido IS NOT NULL
  AND adjunto_tamano IS NULL;

-- Después: debe dar 0.
SELECT count(*) AS adjuntos_sin_tamano_despues
FROM mensajes_incidencia
WHERE adjunto_contenido IS NOT NULL AND adjunto_tamano IS NULL;

-- Control: el tamaño calculado coincide con los bytes reales decodificados.
-- (decode() lee el adjunto completo; es solo para esta verificación única.)
SELECT count(*) AS tamanos_incorrectos
FROM mensajes_incidencia
WHERE adjunto_contenido IS NOT NULL
  AND adjunto_tamano <> octet_length(decode(adjunto_contenido, 'base64'));

COMMIT;
