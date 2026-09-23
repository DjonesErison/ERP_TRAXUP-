package com.traxup.tplug.erp.trial;

import com.traxup.tplug.erp.trial.api.*;
import com.traxup.tplug.erp.usuario.*;
import com.traxup.tplug.erp.tenant.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class TrialApplicationService {
  private final JdbcTemplate jdbc;
  private final UsuarioRepository usuarios;
  private final PasswordEncoder encoder;
  public TrialApplicationService(JdbcTemplate jdbc, UsuarioRepository usuarios, PasswordEncoder encoder) {
    this.jdbc=jdbc; this.usuarios=usuarios; this.encoder=encoder;
  }

  @Transactional
  public TrialResponse criar(CriarTrialRequest r) {
    String email=r.email().trim().toLowerCase(Locale.ROOT);
    if (jdbc.queryForObject("select count(*) from usuarios where lower(email)=?", Integer.class, email) > 0)
      throw new IllegalArgumentException("Ja existe uma conta cadastrada com este e-mail.");
    UUID tenantId=UUID.randomUUID(), empresaId=UUID.randomUUID(), filialId=UUID.randomUUID();
    jdbc.update("insert into tenants(id,nome,ativo) values (?,?,true)", tenantId,r.empresa().trim());
    String doc=limparDocumento(r.documento());
    jdbc.update("insert into empresas(id,tenant_id,razao_social,nome_fantasia,cnpj,ativo) values (?,?,?,?,?,true)",
      empresaId,tenantId,r.empresa().trim(),r.empresa().trim(),doc);
    jdbc.update("insert into filiais(id,tenant_id,empresa_id,nome,cnpj,ativo) values (?,?,?,?,?,true)",
      filialId,tenantId,empresaId,"Matriz",doc);
    Tenant tenant=new Tenant(r.empresa().trim());
    // Mantem o tenant gerenciado pela mesma transacao para criar o usuario com hash BCrypt.
    Tenant persistedTenant = jdbc.query("select id,nome,ativo,criado_em,atualizado_em from tenants where id=?",
      rs -> { rs.next(); return new Tenant(rs.getString("nome")); }, tenantId);
    UUID usuarioId=UUID.randomUUID();
    jdbc.update("insert into usuarios(id,tenant_id,nome,email,senha_hash,ativo) values (?,?,?,?,?,true)",
      usuarioId,tenantId,r.responsavel().trim(),email,encoder.encode(r.senha()));
    UUID perfilId=UUID.randomUUID();
    jdbc.update("insert into perfis(id,tenant_id,nome,descricao,ativo) values (?,?,?,'Administrador do trial',true)",
      perfilId,tenantId,"ADMIN");
    jdbc.update("insert into perfil_permissoes(tenant_id,perfil_id,permissao_id) select ?,?,id from permissoes",
      tenantId,perfilId);
    jdbc.update("insert into usuario_perfis(tenant_id,usuario_id,perfil_id) values (?,?,?)",tenantId,usuarioId,perfilId);
    Instant expira=Instant.now().plus(7, ChronoUnit.DAYS);
    UUID trialId=UUID.randomUUID();
    jdbc.update("insert into trials(id,tenant_id,empresa_id,filial_id,usuario_id,documento,telefone,segmento,quantidade_lojas,expira_em) values (?,?,?,?,?,?,?,?,?,?)",
      trialId,tenantId,empresaId,filialId,usuarioId,doc,r.telefone().trim(),r.segmento(),r.quantidadeLojas()==null?1:r.quantidadeLojas(),Timestamp.from(expira));
    return new TrialResponse(trialId,tenantId,empresaId,filialId,email,expira,"PRIMEIRO_ACESSO");
  }
  private String limparDocumento(String d){ if(d==null||d.isBlank()) return null; String x=d.replaceAll("\\D",""); return x.isBlank()?null:x; }
}
