#!/usr/bin/env bash
set -Eeuo pipefail
cd /opt/traxup-homologacao
sha="${1:?SHA obrigatorio}"
backend="${2:?Digest backend obrigatorio}"
frontend="${3:?Digest frontend obrigatorio}"
[[ "$sha" =~ ^[a-f0-9]{40}$ ]]
[[ "$backend" =~ ^ghcr.io/djoneserison/traxup-erp-backend@sha256:[a-f0-9]{64}$ ]]
[[ "$frontend" =~ ^ghcr.io/djoneserison/traxup-erp-frontend@sha256:[a-f0-9]{64}$ ]]
test -f .env
# Impede deploy simultaneo, inclusive fora do Actions.
exec 9> .deploy.lock
flock -n 9
export BACKEND_IMAGE="$backend" FRONTEND_IMAGE="$frontend"
docker compose --env-file .env -f compose.yml config --quiet
docker compose --env-file .env -f compose.yml pull
if [[ -f release.env ]]; then cp release.env previous-release.env; fi
docker compose --env-file .env -f compose.yml up -d
# Porta efetiva vem da configuracao Compose, sem executar o arquivo de segredos.
port=$(docker compose --env-file .env -f compose.yml port frontend 80)
bash smoke.sh "http://$port" "$sha"
printf 'BACKEND_IMAGE=%s\nFRONTEND_IMAGE=%s\nRELEASE_SHA=%s\n' "$backend" "$frontend" "$sha" > release.env
