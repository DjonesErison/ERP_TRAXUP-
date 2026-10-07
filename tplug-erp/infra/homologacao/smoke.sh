#!/usr/bin/env bash
set -Eeuo pipefail
url="${1:?URL obrigatoria}"
sha="${2:?SHA obrigatorio}"
metadata=''
status=''
for attempt in {1..60}; do
  metadata=$(curl -fsS --max-time 5 "$url/build-info.json" || true)
  status=$(curl -sS -o /dev/null -w '%{http_code}' --max-time 5 -H 'Content-Type: application/json' -d '{}' "$url/api/v1/auth/login" || true)
  if [[ "$metadata" == "{\"commit\":\"$sha\"}" && "$status" == 400 ]]; then
    curl -fsS --max-time 5 "$url/" > /dev/null
    echo "Frontend da versao esperada e API de autenticacao respondendo. commit=$sha http=$status"
    exit 0
  fi
  if (( attempt == 1 || attempt % 10 == 0 || attempt == 60 )); then
    printf 'Smoke tentativa %d/60: esperado=%s metadata=%q api_http=%q url=%s\n' "$attempt" "$sha" "$metadata" "$status" "$url" >&2
  fi
  sleep 3
done
echo 'Smoke falhou: frontend/SHA ou API indisponivel.' >&2
printf 'Diagnostico final: esperado=%s metadata=%q api_http=%q url=%s\n' "$sha" "$metadata" "$status" "$url" >&2
exit 1
