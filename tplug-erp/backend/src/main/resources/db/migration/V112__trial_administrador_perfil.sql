-- Bootstrap only the explicitly registered administrator, within the trial's tenant.
-- Existing role assignments remain under the administrator's control.
CREATE OR REPLACE FUNCTION provisionar_administrador_trial(p_tenant UUID, p_usuario UUID)
RETURNS VOID LANGUAGE plpgsql AS $$
DECLARE v_perfil UUID;
BEGIN
 IF EXISTS(SELECT 1 FROM usuario_perfis WHERE tenant_id=p_tenant AND usuario_id=p_usuario) THEN
  RETURN;
 END IF;
 INSERT INTO perfis(id,tenant_id,nome,descricao,ativo)
 VALUES(gen_random_uuid(),p_tenant,'ADMIN','Administrador da empresa',true)
 ON CONFLICT(tenant_id,nome) DO NOTHING;
 SELECT id INTO v_perfil FROM perfis WHERE tenant_id=p_tenant AND nome='ADMIN' AND ativo=true;
 IF v_perfil IS NULL THEN RETURN; END IF;
 INSERT INTO perfil_permissoes(tenant_id,perfil_id,permissao_id)
 SELECT p_tenant,v_perfil,id FROM permissoes ON CONFLICT DO NOTHING;
 INSERT INTO usuario_perfis(tenant_id,usuario_id,perfil_id)
 VALUES(p_tenant,p_usuario,v_perfil) ON CONFLICT DO NOTHING;
END;
$$;
SELECT provisionar_administrador_trial(tenant_id,administrador_id) FROM trials_saas;
CREATE OR REPLACE FUNCTION provisionar_administrador_novo_trial()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 PERFORM provisionar_administrador_trial(NEW.tenant_id,NEW.administrador_id);
 RETURN NEW;
END;
$$;
CREATE TRIGGER trg_trial_administrador_perfil AFTER INSERT ON trials_saas
FOR EACH ROW EXECUTE FUNCTION provisionar_administrador_novo_trial();
