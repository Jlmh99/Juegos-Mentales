#!/usr/bin/env bash
# =====================================================================
# Pruebas de /api/incidencias con curl (Git Bash).
# Uso:   bash pruebas/probar_incidencias.sh [URL]      (por defecto http://localhost:8080)
#
# 1ª vez: registra 4 cuentas de prueba y se detiene pidiendo que ejecutes
#         sql/roles_prueba.sql (ADMIN y AGENTE). 2ª vez: corre todas las pruebas.
# Los tokens se guardan en variables y NUNCA se imprimen.
# =====================================================================
BASE="${1:-http://localhost:8080}"
PASS='Prueba123!'
CUERPO_TMP="$(mktemp)"
trap 'rm -f "$CUERPO_TMP" "$CUERPO_TMP".*' EXIT

OK=0; FALLOS=0
esperar() { # nombre esperado obtenido
  if [ "$2" = "$3" ]; then echo "  OK     $1 -> $3"; OK=$((OK + 1));
  else echo "  FALLO  $1 -> esperado $2, obtenido $3"; FALLOS=$((FALLOS + 1)); fi
}
# peticion METODO RUTA TOKEN [JSON | @archivo]  -> deja $CODIGO y el cuerpo en $CUERPO
peticion() {
  local args=(-s -o "$CUERPO_TMP" -w "%{http_code}" -X "$1" "$BASE$2" -H "Content-Type: application/json")
  [ -n "$3" ] && args+=(-H "Authorization: Bearer $3")
  # El cuerpo siempre se envía desde un archivo: en Windows, curl convierte los
  # argumentos a la codificación del sistema y rompe los acentos (UTF-8).
  if [ -n "$4" ]; then
    if [ "${4:0:1}" = "@" ]; then args+=(--data-binary "$4")
    else printf '%s' "$4" > "$CUERPO_TMP.envio"; args+=(--data-binary "@$CUERPO_TMP.envio"); fi
  fi
  CODIGO=$(curl "${args[@]}"); CUERPO=$(cat "$CUERPO_TMP")
}
campo() { echo "$CUERPO" | sed -nE "s/^\{\"$1\":\"?([^\",}]*)\"?.*/\1/p"; }        # primer campo del JSON
version() { echo "$CUERPO" | sed -nE 's/.*"version":([0-9]+)\}$/\1/p'; }           # version (último campo)
login() { # email -> token
  curl -s -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" \
    -d "{\"email\":\"$1\",\"password\":\"$PASS\"}" | sed -nE 's/.*"token":"([^"]+)".*/\1/p'
}

echo "== Registro de cuentas de prueba (si ya existen, no pasa nada)"
for u in usuario.a usuario.b agente admin; do
  curl -s -o /dev/null -X POST "$BASE/api/auth/register" -H "Content-Type: application/json" \
    -d "{\"username\":\"${u/./_}\",\"email\":\"$u@prueba.com\",\"password\":\"$PASS\"}"
done
TA=$(login usuario.a@prueba.com); TB=$(login usuario.b@prueba.com)
TAG=$(login agente@prueba.com);   TAD=$(login admin@prueba.com)
if [ -z "$TA" ] || [ -z "$TB" ] || [ -z "$TAG" ] || [ -z "$TAD" ]; then
  echo "No se pudo iniciar sesión con alguna cuenta (¿tienen 2FA activo?)."; exit 1
fi
peticion GET /api/incidencias/agentes "$TAD"
if [ "$CODIGO" != "200" ] || ! echo "$CUERPO" | grep -q agente@prueba.com; then
  echo ">> Falta asignar roles. Ejecuta sql/roles_prueba.sql y vuelve a correr este script."; exit 2
fi
ID_AGENTE=$(echo "$CUERPO" | sed -nE 's/.*\{"id":([0-9]+),"username":"[^"]*","email":"agente@prueba.com".*/\1/p')

echo "== 1. Creación por dos usuarios distintos"
peticion POST /api/incidencias "$TA" '{"titulo":"No carga el Sudoku","descripcion":"Se queda en blanco","prioridad":"ALTA"}'
esperar "A crea su incidencia" 201 "$CODIGO"; ID_A=$(campo id); VER_A=$(version)
peticion POST /api/incidencias "$TB" '{"titulo":"Error en crucigrama","descripcion":"No valida la palabra"}'
esperar "B crea su incidencia" 201 "$CODIGO"; ID_B=$(campo id)

echo "== 2. creadoPor ajeno"
peticion POST /api/incidencias "$TA" '{"titulo":"Suplantar","descripcion":"x","creadoPor":"usuario.b@prueba.com"}'
esperar "A crea a nombre de B" 403 "$CODIGO"

echo "== 3. Sin token"
peticion GET /api/incidencias ""; esperar "GET sin token" 403 "$CODIGO"
peticion POST /api/incidencias "" '{"titulo":"x","descripcion":"x"}'; esperar "POST sin token" 403 "$CODIGO"
peticion GET "/api/incidencias/$ID_A/mensajes" ""; esperar "Mensajes sin token" 403 "$CODIGO"

echo "== 4. A no puede ver, modificar ni borrar la incidencia de B"
peticion GET "/api/incidencias/$ID_B" "$TA"; esperar "A ve la de B" 403 "$CODIGO"
peticion PUT "/api/incidencias/$ID_B" "$TA" '{"titulo":"Hackeado","version":0}'; esperar "A modifica la de B" 403 "$CODIGO"
peticion DELETE "/api/incidencias/$ID_B" "$TA"; esperar "A borra la de B" 403 "$CODIGO"
peticion GET /api/incidencias "$TA"
echo "$CUERPO" | grep -q "\"id\":$ID_B," && esperar "Lista de A sin la de B" "sin B" "con B" || esperar "Lista de A sin la de B" "sin B" "sin B"

echo "== 5. Conflicto 409 con dos versiones"
peticion PUT "/api/incidencias/$ID_A" "$TA" "{\"titulo\":\"No carga el Sudoku (editado)\",\"version\":$VER_A}"
esperar "Dispositivo 1 guarda con version $VER_A" 200 "$CODIGO"
peticion PUT "/api/incidencias/$ID_A" "$TA" "{\"descripcion\":\"Edición del dispositivo 2\",\"version\":$VER_A}"
esperar "Dispositivo 2 guarda con la versión vieja" 409 "$CODIGO"; echo "         mensaje: $(campo mensaje)"
peticion PUT "/api/incidencias/$ID_A" "$TA" '{"titulo":"Sin version"}'; esperar "PUT sin version" 400 "$CODIGO"

echo "== 6. Asignación a agente"
peticion GET /api/incidencias/agentes "$TA"; esperar "USER lista agentes" 403 "$CODIGO"
peticion PUT "/api/incidencias/$ID_A/asignar" "$TA" "{\"agenteId\":$ID_AGENTE}"; esperar "USER asigna" 403 "$CODIGO"
peticion PUT "/api/incidencias/$ID_A/asignar" "$TAD" "{\"agenteId\":$ID_AGENTE}"; esperar "ADMIN asigna al agente" 200 "$CODIGO"
VER_A=$(version)
peticion GET "/api/incidencias/$ID_B" "$TAG"; esperar "Agente ve una no asignada" 403 "$CODIGO"

echo "== 7. Permisos por campo en PUT"
peticion PUT "/api/incidencias/$ID_A" "$TAG" "{\"estado\":\"EN_PROCESO\",\"version\":$VER_A}"
esperar "Agente cambia el estado" 200 "$CODIGO"; VER_A=$(version)
peticion PUT "/api/incidencias/$ID_A" "$TAG" "{\"titulo\":\"Cambio del agente\",\"version\":$VER_A}"; esperar "Agente cambia el título" 403 "$CODIGO"
peticion PUT "/api/incidencias/$ID_A" "$TA" "{\"estado\":\"RESUELTA\",\"version\":$VER_A}"; esperar "Creador cambia el estado" 403 "$CODIGO"
peticion PUT "/api/incidencias/$ID_A" "$TA" "{\"prioridad\":\"BAJA\",\"version\":$VER_A}"; esperar "Creador cambia la prioridad" 403 "$CODIGO"
peticion PUT "/api/incidencias/$ID_A" "$TAD" "{\"prioridad\":\"CRITICA\",\"version\":$VER_A}"; esperar "ADMIN cambia la prioridad" 200 "$CODIGO"

echo "== 8. Mensajes (canales PUBLICO e INTERNO)"
PNG='iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=='
PDF=$(printf '%%PDF-1.4\n%%%%EOF\n' | base64 -w0)
M="/api/incidencias/$ID_A/mensajes"
peticion POST "$M" "$TA" '{"texto":"Hola, sigue sin cargar"}'; esperar "Creador escribe (PUBLICO por defecto)" 201 "$CODIGO"
MID_TEXTO=$(campo id)
peticion POST "$M" "$TA" "{\"texto\":\"Captura\",\"adjunto\":{\"nombre\":\"captura.png\",\"tipo\":\"image/png\",\"contenido\":\"$PNG\"}}"
esperar "Mensaje con adjunto PNG" 201 "$CODIGO"; MID_PNG=$(campo id)
peticion POST "$M" "$TAG" "{\"texto\":\"Reporte\",\"adjunto\":{\"nombre\":\"r.pdf\",\"tipo\":\"application/pdf\",\"contenido\":\"$PDF\"}}"
esperar "Mensaje con adjunto PDF" 201 "$CODIGO"
peticion POST "$M" "$TA" '{"texto":"Quiero ver lo interno","canal":"INTERNO"}'; esperar "Creador escribe en INTERNO" 403 "$CODIGO"
peticion GET "$M?canal=INTERNO" "$TA"; esperar "Creador lee INTERNO" 403 "$CODIGO"
peticion POST "$M" "$TAG" '{"texto":"Admin, ¿lo escalamos?","canal":"INTERNO"}'; esperar "Agente escribe en INTERNO" 201 "$CODIGO"
peticion POST "$M" "$TAD" '{"texto":"Sí, prioridad crítica","canal":"INTERNO"}'; esperar "ADMIN escribe en INTERNO" 201 "$CODIGO"
peticion GET "$M?canal=INTERNO" "$TAG"; esperar "Agente lee INTERNO" 200 "$CODIGO"
peticion GET "$M" "$TA"; esperar "Creador lee PUBLICO" 200 "$CODIGO"
peticion GET "$M" "$TB"; esperar "Otro usuario lee mensajes ajenos" 403 "$CODIGO"
peticion POST "$M" "$TA" '{"texto":"   "}'; esperar "Texto vacío" 400 "$CODIGO"
peticion POST "$M" "$TA" "{\"texto\":\"$(head -c 1001 /dev/zero | tr '\0' 'a')\"}"; esperar "Texto de 1001 caracteres" 400 "$CODIGO"
peticion POST "$M" "$TA" '{"texto":"exe","adjunto":{"nombre":"virus.exe","tipo":"application/x-msdownload","contenido":"TVqQAAMAAAAEAAAA"}}'
esperar "Adjunto de tipo no permitido" 400 "$CODIGO"
peticion POST "$M" "$TA" "{\"texto\":\"falso\",\"adjunto\":{\"nombre\":\"x.png\",\"tipo\":\"image/png\",\"contenido\":\"$PDF\"}}"
esperar "PDF disfrazado de PNG" 400 "$CODIGO"
{ printf '{"texto":"grande","adjunto":{"nombre":"g.png","tipo":"image/png","contenido":"'
  { printf '\x89PNG\r\n\x1a\n'; head -c 2200000 /dev/zero; } | base64 -w0; printf '"}}'; } > "$CUERPO_TMP.grande"
peticion POST "$M" "$TA" "@$CUERPO_TMP.grande"; esperar "Adjunto de más de 2 MB" 400 "$CODIGO"

echo "== 8b. Adjuntos fuera del listado (Práctica 12)"
# descargar RUTA TOKEN -> deja $CODIGO, $TIPO (Content-Type) y el archivo en $CUERPO_TMP.bin
descargar() {
  local args=(-s -o "$CUERPO_TMP.bin" -D "$CUERPO_TMP.cab" -w "%{http_code}" "$BASE$1")
  [ -n "$2" ] && args+=(-H "Authorization: Bearer $2")
  CODIGO=$(curl "${args[@]}")
  TIPO=$(tr -d '\r' < "$CUERPO_TMP.cab" | sed -nE 's/^[Cc]ontent-[Tt]ype: *([^;]*).*/\1/p' | tail -1)
}
peticion POST "$M" "$TAG" "{\"texto\":\"Captura interna\",\"canal\":\"INTERNO\",\"adjunto\":{\"nombre\":\"i.png\",\"tipo\":\"image/png\",\"contenido\":\"$PNG\"}}"
esperar "Agente escribe en INTERNO con adjunto" 201 "$CODIGO"; MID_INTERNO=$(campo id)
peticion POST "/api/incidencias/$ID_B/mensajes" "$TB" "{\"texto\":\"De B\",\"adjunto\":{\"nombre\":\"b.png\",\"tipo\":\"image/png\",\"contenido\":\"$PNG\"}}"
esperar "B escribe en su incidencia con adjunto" 201 "$CODIGO"; MID_DE_B=$(campo id)

peticion GET "$M" "$TA"
echo "$CUERPO" | grep -q '"contenido"' && esperar "Listado sin \"contenido\"" "sin contenido" "con contenido" \
                                       || esperar "Listado sin \"contenido\"" "sin contenido" "sin contenido"
echo "$CUERPO" | grep -qE "\"id\":$MID_PNG,\"incidenciaId\":[0-9]+,\"emisor\":\{[^}]*\},\"texto\":\"[^\"]*\",\"canal\":\"PUBLICO\",\"fecha\":\"[^\"]*\",\"tieneAdjunto\":true" && esperar "Listado marca tieneAdjunto" si si || esperar "Listado marca tieneAdjunto" si no
echo "$CUERPO" | grep -q '"tamano":70' && esperar "Listado trae el tamaño real del PNG (70 bytes)" si si || esperar "Listado trae el tamaño real del PNG (70 bytes)" si no
# Fase 6b: el tamaño sale de la columna adjunto_tamano (calculada al enviar), no del Base64.
echo "$CUERPO" | grep -q '"nombre":"r.pdf","tipo":"application/pdf","tamano":15}' && esperar "Listado trae el tamaño real del PDF (15 bytes)" si si || esperar "Listado trae el tamaño real del PDF (15 bytes)" si no

descargar "$M/$MID_PNG/adjunto" "$TA"; esperar "Creador baja adjunto PUBLICO" 200 "$CODIGO"
esperar "Content-Type del adjunto" image/png "$TIPO"
cmp -s "$CUERPO_TMP.bin" <(printf '%s' "$PNG" | base64 -d) && esperar "Bytes iguales al PNG enviado" si si || esperar "Bytes iguales al PNG enviado" si no
descargar "$M/$MID_INTERNO/adjunto" "$TA"; esperar "Creador baja adjunto INTERNO" 403 "$CODIGO"
descargar "$M/$MID_INTERNO/adjunto" "$TAG"; esperar "Agente asignado baja adjunto INTERNO" 200 "$CODIGO"
descargar "$M/$MID_PNG/adjunto" "$TB"; esperar "Otro usuario baja adjunto ajeno" 403 "$CODIGO"
descargar "$M/$MID_DE_B/adjunto" "$TA"; esperar "mid de otra incidencia" 404 "$CODIGO"
descargar "$M/$MID_DE_B/adjunto" "$TAD"; esperar "mid de otra incidencia (ADMIN)" 404 "$CODIGO"
descargar "$M/$MID_TEXTO/adjunto" "$TA"; esperar "Mensaje sin adjunto" 404 "$CODIGO"
descargar "$M/999999999/adjunto" "$TA"; esperar "Mensaje inexistente" 404 "$CODIGO"
descargar "$M/$MID_PNG/adjunto" ""; esperar "Adjunto sin token" 403 "$CODIGO"

echo "== 9. Borrado"
peticion DELETE "/api/incidencias/$ID_A" "$TAG"; esperar "Agente borra" 403 "$CODIGO"
peticion DELETE "/api/incidencias/$ID_A" "$TA"; esperar "Creador borra la suya" 200 "$CODIGO"
peticion DELETE "/api/incidencias/$ID_B" "$TAD"; esperar "ADMIN borra la de B" 200 "$CODIGO"
peticion GET "/api/incidencias/$ID_A" "$TAD"; esperar "La incidencia borrada ya no existe" 404 "$CODIGO"

echo; echo "Resultado: $OK correctas, $FALLOS fallidas"
[ "$FALLOS" -eq 0 ]
