package com.traxup.tplug.erp.trial;

import com.traxup.tplug.erp.trial.api.CriarTrialRequest;
import com.traxup.tplug.erp.trial.api.TrialResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Service
public class TrialApplicationService {
  private final JdbcTemplate jdbc;
  private final PasswordEncoder encoder;

  public TrialApplicationService(JdbcTemplate jdbc, PasswordEncoder encoder) {
    this.jdbc = jdbc;
    this.encoder = encoder;
  }

  @Transactional
  public TrialResponse criar(CriarTrialRequest r) {
    String email = r.email().trim().toLowerCase(Locale.ROOT);
    Integer existentes = jdbc.queryForObject("select count(*) from usuarios where lower(email)=?", Integer.class, email);
    if (existentes != null && existentes > 0) {
      throw new IllegalArgumentException("Ja existe uma conta cadastrada com este e-mail.");
    }

    UUID tenantId = UUID.randomUUID();
    UUID empresaId = UUID.randomUUID();
    UUID filialId = UUID.randomUUID();
    UUID usuarioId = UUID.randomUUID();
    UUID perfilId = UUID.randomUUID();
    UUID trialId = UUID.randomUUID();
    String doc = limparDocumento(r.documento());

    jdbc.update("insert into tenants(id,nome,ativo) values (?,?,true)", tenantId, r.empresa().trim());
    String razaoSocial = r.razaoSocial() == null || r.razaoSocial().isBlank()
        ? r.empresa().trim() : r.razaoSocial().trim();
    jdbc.update("insert into empresas(id,tenant_id,razao_social,nome_fantasia,cnpj,ativo) values (?,?,?,?,?,true)",
        empresaId, tenantId, razaoSocial, r.empresa().trim(), doc);
    jdbc.update("insert into filiais(id,tenant_id,empresa_id,nome,cnpj,ativo) values (?,?,?,?,?,true)",
        filialId, tenantId, empresaId, "Matriz", doc);
    jdbc.update("insert into usuarios(id,tenant_id,nome,email,senha_hash,ativo) values (?,?,?,?,?,true)",
        usuarioId, tenantId, r.responsavel().trim(), email, encoder.encode(r.senha()));
    jdbc.update("insert into perfis(id,tenant_id,nome,descricao,ativo) values (?,?,?,'Administrador do trial',true)",
        perfilId, tenantId, "ADMIN");
    jdbc.update("insert into perfil_permissoes(tenant_id,perfil_id,permissao_id) select ?,?,id from permissoes",
        tenantId, perfilId);
    jdbc.update("insert into usuario_perfis(tenant_id,usuario_id,perfil_id) values (?,?,?)",
        tenantId, usuarioId, perfilId);

    Instant expira = Instant.now().plus(7, ChronoUnit.DAYS);
    jdbc.update("insert into trials(id,tenant_id,empresa_id,filial_id,usuario_id,documento,telefone,segmento,quantidade_lojas,expira_em) values (?,?,?,?,?,?,?,?,?,?)",
        trialId, tenantId, empresaId, filialId, usuarioId, doc, r.telefone().trim(), r.segmento(),
        r.quantidadeLojas() == null ? 1 : r.quantidadeLojas(), Timestamp.from(expira));

    return new TrialResponse(trialId, tenantId, empresaId, filialId, email, expira, "PRIMEIRO_ACESSO");
  }

  private String limparDocumento(String documento) {
    if (documento == null || documento.isBlank()) return null;
    String limpo = documento.replaceAll("\\D", "");
    return limpo.isBlank() ? null : limpo;
  }
}
