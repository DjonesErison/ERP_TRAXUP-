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
docker compose --env-file .env -f compose.yml -f compose.mail.yml config --quiet
docker compose --env-file .env -f compose.yml -f compose.mail.yml pull
# The application runs as UID 10001. Prepare only file ownership/permissions,
# without reading or exposing the mailbox password in logs or environment variables.
docker run --rm --user 0 --entrypoint sh \
  --mount type=bind,src=/opt/traxup-homologacao/secrets/smtp-password,dst=/smtp-password \
  "$backend" -c 'test -s /smtp-password && chown 10001:10001 /smtp-password && chmod 400 /smtp-password'
if [[ -f release.env ]]; then cp release.env previous-release.env; fi
rollback() {
  code=$?
  trap - ERR
  if [[ -f previous-release.env ]]; then
    old_backend=$(sed -n 's/^BACKEND_IMAGE=//p' previous-release.env)
    old_frontend=$(sed -n 's/^FRONTEND_IMAGE=//p' previous-release.env)
    if [[ "$old_backend" =~ ^ghcr.io/djoneserison/traxup-erp-backend@sha256:[a-f0-9]{64}$ && "$old_frontend" =~ ^ghcr.io/djoneserison/traxup-erp-frontend@sha256:[a-f0-9]{64}$ ]]; then
      echo 'Deploy failed; restoring previous images.'
      BACKEND_IMAGE="$old_backend" FRONTEND_IMAGE="$old_frontend" docker compose --env-file .env -f compose.yml up -d || true
    fi
  fi
  exit "$code"
}
trap rollback ERR
bash cleanup-simulations.sh
docker compose --env-file .env -f compose.yml -f compose.mail.yml up -d
# Porta efetiva vem da configuracao Compose, sem executar o arquivo de segredos.
port=$(docker compose --env-file .env -f compose.yml -f compose.mail.yml port frontend 80)
bash smoke.sh "http://$port" "$sha"
trap - ERR
printf 'BACKEND_IMAGE=%s\nFRONTEND_IMAGE=%s\nRELEASE_SHA=%s\n' "$backend" "$frontend" "$sha" > release.env
