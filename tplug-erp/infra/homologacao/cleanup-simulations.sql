\set ON_ERROR_STOP on
BEGIN;
DO $$ BEGIN
  IF current_database() <> 'traxup_homologacao' THEN RAISE EXCEPTION 'Homologacao only'; END IF;
END $$;
CREATE TABLE IF NOT EXISTS traxup_maintenance_runs (
  operation TEXT PRIMARY KEY, completed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), summary JSONB NOT NULL
);
CREATE TEMP TABLE cleanup_tenants AS
SELECT id FROM tenants WHERE id IN (SELECT tenant_id FROM trials_saas UNION SELECT tenant_id FROM empresas);
-- Preserve standalone administrative bootstrap tenants and all global technical catalogs.
CREATE TEMP TABLE cleanup_rows (rel OID, row_tid TID, PRIMARY KEY(rel,row_tid));
CREATE TEMP TABLE cleanup_counts (table_name TEXT PRIMARY KEY, removed BIGINT);
DO $$
DECLARE r RECORD; n BIGINT; total BIGINT; progress BIGINT; predicate TEXT;
BEGIN
  IF EXISTS (SELECT 1 FROM traxup_maintenance_runs WHERE operation='trial-simulations-20260923-v1') THEN RETURN; END IF;
  INSERT INTO cleanup_rows SELECT 'tenants'::regclass,ctid FROM tenants WHERE id IN (SELECT id FROM cleanup_tenants);
  FOR r IN SELECT c.oid,c.relname FROM pg_class c JOIN pg_namespace s ON s.oid=c.relnamespace
           JOIN pg_attribute a ON a.attrelid=c.oid AND a.attname='tenant_id' AND NOT a.attisdropped
           WHERE s.nspname='public' AND c.relkind='r' LOOP
    EXECUTE format('INSERT INTO cleanup_rows SELECT %s,ctid FROM %I WHERE tenant_id IN (SELECT id FROM cleanup_tenants) ON CONFLICT DO NOTHING',r.oid,r.relname);
  END LOOP;
  -- Follow actual foreign keys, including token/email/junction tables lacking tenant_id.
  LOOP
    total:=0;
    FOR r IN SELECT k.*,c.relname AS child,p.relname AS parent FROM pg_constraint k
             JOIN pg_class c ON c.oid=k.conrelid JOIN pg_class p ON p.oid=k.confrelid
             WHERE k.contype='f' AND c.relnamespace='public'::regnamespace LOOP
      SELECT string_agg(format('c.%I=p.%I',ca.attname,pa.attname),' AND ') INTO predicate
      FROM unnest(r.conkey,r.confkey) AS x(childnum,parentnum)
      JOIN pg_attribute ca ON ca.attrelid=r.conrelid AND ca.attnum=x.childnum
      JOIN pg_attribute pa ON pa.attrelid=r.confrelid AND pa.attnum=x.parentnum;
      EXECUTE format('INSERT INTO cleanup_rows SELECT %s,c.ctid FROM %I c JOIN %I p ON %s JOIN cleanup_rows d ON d.rel=%s AND d.row_tid=p.ctid ON CONFLICT DO NOTHING',r.conrelid,r.child,r.parent,predicate,r.confrelid);
      GET DIAGNOSTICS n=ROW_COUNT; total:=total+n;
    END LOOP;
    EXIT WHEN total=0;
  END LOOP;
  -- The dependency closure must never include global permissions or Flyway history.
  IF EXISTS(SELECT 1 FROM cleanup_rows WHERE rel IN ('permissoes'::regclass,'flyway_schema_history'::regclass)) THEN
    RAISE EXCEPTION 'Protected technical rows in cleanup closure';
  END IF;
  LOOP
    progress:=0;
    FOR r IN SELECT d.rel,c.relname,count(*) AS amount FROM cleanup_rows d JOIN pg_class c ON c.oid=d.rel GROUP BY d.rel,c.relname LOOP
      BEGIN
        EXECUTE format('DELETE FROM %I t USING cleanup_rows d WHERE d.rel=%s AND d.row_tid=t.ctid',r.relname,r.rel);
        GET DIAGNOSTICS n=ROW_COUNT;
        INSERT INTO cleanup_counts VALUES(r.relname,n) ON CONFLICT(table_name) DO UPDATE SET removed=cleanup_counts.removed+EXCLUDED.removed;
        DELETE FROM cleanup_rows WHERE rel=r.rel;
        progress:=progress+1;
      EXCEPTION WHEN foreign_key_violation THEN NULL;
      END;
    END LOOP;
    EXIT WHEN NOT EXISTS(SELECT 1 FROM cleanup_rows);
    IF progress=0 THEN RAISE EXCEPTION 'Unresolved dependency; cleanup rolled back'; END IF;
  END LOOP;
  IF EXISTS(SELECT 1 FROM trials_saas) THEN RAISE EXCEPTION 'Trials remain'; END IF;
  INSERT INTO traxup_maintenance_runs(operation,summary)
    SELECT 'trial-simulations-20260923-v1',COALESCE(jsonb_object_agg(table_name,removed),'{}'::jsonb) FROM cleanup_counts;
END $$;
SELECT operation,completed_at,summary FROM traxup_maintenance_runs WHERE operation='trial-simulations-20260923-v1';
COMMIT;
