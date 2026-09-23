#!/usr/bin/env bash
set -Eeuo pipefail
# Called only under deploy.sh's exclusive lock, before the new migration is run.
[[ "$PWD" == /opt/traxup-homologacao ]]
umask 077
compose=(docker compose --env-file .env -f compose.yml -f compose.mail.yml)
id=$("${compose[@]}" ps -q postgres)
[[ -n "$id" ]]
[[ "$(docker inspect -f '{{index .Config.Labels "com.docker.compose.project"}}' "$id")" == traxup-homologacao ]]
psql_cmd=(docker exec -i "$id" psql -X -U traxup_homologacao -d traxup_homologacao -v ON_ERROR_STOP=1)
[[ "$("${psql_cmd[@]}" -Atc 'SELECT current_database()')" == traxup_homologacao ]]
if [[ "$("${psql_cmd[@]}" -Atc "SELECT to_regclass('public.traxup_maintenance_runs') IS NOT NULL")" == t ]]; then
  if [[ "$("${psql_cmd[@]}" -Atc "SELECT count(*) FROM traxup_maintenance_runs WHERE operation='trial-simulations-20260923-v1'")" == 1 ]]; then
    echo 'Simulation cleanup already completed; new registrations preserved.'
    exit 0
  fi
fi
"${compose[@]}" stop backend
trap '"${compose[@]}" start backend' EXIT
backup_dir="backups/trial-simulations-$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$backup_dir"
docker exec "$id" pg_dump -U traxup_homologacao -d traxup_homologacao -Fc > "$backup_dir/database.dump"
test -s "$backup_dir/database.dump"
sha256sum "$backup_dir/database.dump" > "$backup_dir/SHA256SUMS"
docker exec -i "$id" pg_restore --list < "$backup_dir/database.dump" > "$backup_dir/contents.txt"
# Validate the dump with an actual restore into an isolated database in the homologation container.
verify_db="traxup_verify_$(date +%s)"
docker exec "$id" createdb -U traxup_homologacao "$verify_db"
if ! docker exec -i "$id" pg_restore --exit-on-error -U traxup_homologacao -d "$verify_db" < "$backup_dir/database.dump"; then
  docker exec "$id" dropdb -U traxup_homologacao "$verify_db"
  exit 1
fi
docker exec "$id" dropdb -U traxup_homologacao "$verify_db"
"${psql_cmd[@]}" < cleanup-simulations.sql | tee "$backup_dir/cleanup-summary.txt"
printf 'Backup verified by restore: /opt/traxup-homologacao/%s/database.dump\n' "$backup_dir"
cat "$backup_dir/SHA256SUMS"
# deploy.sh starts the new images next. Do not restart old code after successful cleanup.
trap - EXIT
