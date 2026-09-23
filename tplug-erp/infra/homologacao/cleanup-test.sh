#!/usr/bin/env bash
set -Eeuo pipefail
# Disposable CI PostgreSQL only; no SSH and no homologation secrets.
export PGHOST=127.0.0.1 PGUSER=tplug_erp PGPASSWORD=tplug_erp_ci
createdb traxup_homologacao
trap 'dropdb traxup_homologacao' EXIT
pg_dump -d tplug_erp --schema-only | psql -X -v ON_ERROR_STOP=1 -d traxup_homologacao >/dev/null
psql -X -v ON_ERROR_STOP=1 -d traxup_homologacao <<'SQL'
INSERT INTO tenant_codigo_empresa_allocator VALUES(true,0);
INSERT INTO tenants(id,nome) VALUES ('10000000-0000-0000-0000-000000000001','Trial simulation'),('10000000-0000-0000-0000-000000000002','Technical bootstrap');
INSERT INTO empresas(id,tenant_id,razao_social) VALUES ('20000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001','Simulation');
INSERT INTO usuarios(id,tenant_id,nome,email,senha_hash) VALUES ('30000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001','Simulation','simulation@example.test','not-a-real-hash');
INSERT INTO trials_saas(id,tenant_id,empresa_id,administrador_id,email,documento,telefone,inicio_em,expira_em,status,termos_versao,termos_aceitos_em,idempotency_key)
VALUES ('40000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000001','simulation@example.test','12345678901','11999999999',NOW(),NOW()+INTERVAL '7 days','ATIVO','v1',NOW(),'cleanup-test');
INSERT INTO trial_activation_emails(trial_id) VALUES('40000000-0000-0000-0000-000000000001');
INSERT INTO access_recovery_emails(usuario_id) VALUES('30000000-0000-0000-0000-000000000001');
INSERT INTO ativacao_admin_tokens(id,tenant_id,usuario_id,token_hash,expira_em) VALUES(gen_random_uuid(),'10000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000001',repeat('a',64),NOW()+INTERVAL '30 minutes');
INSERT INTO permissoes(id,chave,descricao) VALUES(gen_random_uuid(),'TECHNICAL_PRESERVED','Must remain');
SQL
psql -X -v ON_ERROR_STOP=1 -d traxup_homologacao -f ../infra/homologacao/cleanup-simulations.sql
psql -X -v ON_ERROR_STOP=1 -d traxup_homologacao <<'SQL'
DO $$ BEGIN
 IF EXISTS(SELECT 1 FROM trials_saas) OR EXISTS(SELECT 1 FROM empresas) OR EXISTS(SELECT 1 FROM usuarios) OR EXISTS(SELECT 1 FROM trial_activation_emails) OR EXISTS(SELECT 1 FROM access_recovery_emails) OR EXISTS(SELECT 1 FROM ativacao_admin_tokens) THEN RAISE EXCEPTION 'Business rows remain'; END IF;
 IF (SELECT count(*) FROM tenants)<>1 OR (SELECT count(*) FROM permissoes WHERE chave='TECHNICAL_PRESERVED')<>1 THEN RAISE EXCEPTION 'Technical data removed'; END IF;
END $$;
INSERT INTO empresas(id,tenant_id,razao_social) VALUES(gen_random_uuid(),'10000000-0000-0000-0000-000000000002','New registration after cleanup');
SQL
psql -X -v ON_ERROR_STOP=1 -d traxup_homologacao -f ../infra/homologacao/cleanup-simulations.sql
[[ "$(psql -X -At -d traxup_homologacao -c 'SELECT count(*) FROM empresas')" == 1 ]]
# Running against any other database must fail before modifying rows.
if psql -X -d tplug_erp -f ../infra/homologacao/cleanup-simulations.sql; then exit 1; fi
