#!/usr/bin/env bash
set -Eeuo pipefail

IMAGE="${TRAXUP_CENTRAL_IMAGE:-ghcr.io/djoneserison/traxup-central:latest}"
CONTAINER="${TRAXUP_CENTRAL_CONTAINER:-traxup-central}"
PORT="${TRAXUP_CENTRAL_PORT:-8081}"
PUBLIC_URL="${TRAXUP_CENTRAL_PUBLIC_URL:-https://central.traxup.com.br}"

log() { printf '[TRAXUP deploy] %s\n' "$*"; }

log "Baixando imagem ${IMAGE}"
docker pull "$IMAGE"

OLD_IMAGE=""
if docker container inspect "$CONTAINER" >/dev/null 2>&1; then
  OLD_IMAGE="$(docker container inspect --format '{{.Image}}' "$CONTAINER")"
  log "Removendo container anterior ${CONTAINER}"
  docker rm -f "$CONTAINER"
fi

start_container() {
  local image="$1"
  docker run -d \
    --name "$CONTAINER" \
    --restart unless-stopped \
    -p "127.0.0.1:${PORT}:80" \
    "$image" >/dev/null
}

rollback() {
  log "Falha no health check. Iniciando rollback."
  docker rm -f "$CONTAINER" >/dev/null 2>&1 || true
  if [[ -n "$OLD_IMAGE" ]]; then
    start_container "$OLD_IMAGE"
    log "Rollback concluído para a imagem anterior."
  else
    log "Não existe imagem anterior registrada para rollback."
  fi
}

start_container "$IMAGE"

log "Aguardando health check local"
for attempt in {1..20}; do
  if curl --fail --silent --show-error --max-time 5 "http://127.0.0.1:${PORT}/" >/dev/null; then
    log "Health check local OK"
    break
  fi
  if [[ "$attempt" -eq 20 ]]; then
    rollback
    exit 1
  fi
  sleep 2
done

log "Validando domínio público"
if ! curl --fail --silent --show-error --max-time 10 "$PUBLIC_URL" >/dev/null; then
  rollback
  exit 1
fi

log "Deploy concluído com sucesso: ${IMAGE}"
docker ps --filter "name=^/${CONTAINER}$" --format 'container={{.Names}} image={{.Image}} status={{.Status}}'
