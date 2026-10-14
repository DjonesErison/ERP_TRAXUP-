#!/usr/bin/env bash
# Metadados e token recebidos exclusivamente pelo stdin criptografado do SSH.
set +x
set -Eeuo pipefail
IFS= read -r username
IFS= read -r sha
IFS= read -r backend
IFS= read -r frontend
test -n "$username"
[[ "$sha" =~ ^[a-f0-9]{40}$ ]]
[[ "$backend" =~ ^ghcr.io/djoneserison/traxup-erp-backend@sha256:[a-f0-9]{64}$ ]]
[[ "$frontend" =~ ^ghcr.io/djoneserison/traxup-erp-frontend@sha256:[a-f0-9]{64}$ ]]
umask 077
DOCKER_CONFIG="$(mktemp -d /tmp/traxup-homologacao-docker.XXXXXX)"
export DOCKER_CONFIG
trap 'rm -rf "$DOCKER_CONFIG"' EXIT
docker login ghcr.io --username "$username" --password-stdin
bash "$(dirname -- "$0")/deploy.sh" "$sha" "$backend" "$frontend"
