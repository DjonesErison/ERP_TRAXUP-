#!/usr/bin/env bash
set -Eeuo pipefail
url="${1:?URL obrigatoria}"
sha="${2:?SHA obrigatorio}"
for attempt in {1..60}; do
  metadata=$(curl -fsS --max-time 5 "$url/build-info.json" || true)
  status=$(curl -sS -o /dev/null -w '%{http_code}' --max-time 5 -H 'Content-Type: application/json' -d '{}' "$url/api/v1/auth/login" || true)
  if [[ "$metadata" == "{\"commit\":\"$sha\"}" && "$status" == 400 ]]; then
    curl -fsS --max-time 5 "$url/" > /dev/null
    echo 'Frontend da versao esperada e API de autenticacao respondendo.'
    exit 0
  fi
  sleep 3
done
echo 'Smoke falhou: versao ou API indisponivel.' >&2
exit 1
