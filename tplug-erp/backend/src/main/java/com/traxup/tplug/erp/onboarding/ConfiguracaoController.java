package com.traxup.tplug.erp.onboarding;

import com.traxup.tplug.erp.auth.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/onboarding/configuracao")
public class ConfiguracaoController {
 private final JdbcTemplate jdbc;
 private final TenantContext context;
 private final OnboardingService onboarding;
 public ConfiguracaoController(JdbcTemplate jdbc, TenantContext context, OnboardingService onboarding) {
  this.jdbc=jdbc; this.context=context; this.onboarding=onboarding;
 }
 @GetMapping
 public Map<String,Object> dados() {
  UUID t=context.tenantId();
  var empresas=jdbc.queryForList("""
   SELECT e.id, e.razao_social AS "razaoSocial", e.nome_fantasia AS "nomeFantasia", coalesce(t.documento,e.cnpj) AS cnpj,
          t.email, t.telefone, t.segmento, t.quantidade_lojas AS "quantidadeLojas",
          a.nome AS "responsavel", c.cep, c.endereco, c.numero, c.complemento, c.bairro, c.cidade, c.uf,
          coalesce(c.empresa_revisada,false) AS "empresaRevisada",
          coalesce(c.equipe_revisada,false) AS "equipeRevisada"
   FROM empresas e LEFT JOIN trials_saas t ON t.empresa_id=e.id AND t.tenant_id=e.tenant_id
   LEFT JOIN usuarios a ON a.id=t.administrador_id AND a.tenant_id=e.tenant_id
   LEFT JOIN onboarding_configuracoes c ON c.tenant_id=e.tenant_id
   WHERE e.tenant_id=? ORDER BY e.criado_em,e.id LIMIT 1
   """, t);
  Map<String,Object> result=new LinkedHashMap<>();
  result.put("empresa", empresas.isEmpty()?null:empresas.getFirst());
  result.put("nomeUsuario", jdbc.query("select nome from usuarios where tenant_id=? and id=?", (rs,n)->rs.getString(1),t,context.usuarioIdOuNulo()).stream().findFirst().orElse(""));
  var filiais=jdbc.queryForList("select id,nome,cnpj from filiais where tenant_id=? and ativo=true order by criado_em,id limit 1",t);
  UUID filialId=filiais.isEmpty()?null:(UUID)filiais.getFirst().get("id");
  result.put("filial",filiais.isEmpty()?null:filiais.getFirst());
  result.put("fiscalConfigurado", filialId!=null && Boolean.TRUE.equals(jdbc.queryForObject("select exists(select 1 from fiscal_perfis_filial where tenant_id=? and filial_id=? and ativo=true)",Boolean.class,t,filialId)));
  result.put("vendasConfiguradas",filialId!=null && Boolean.TRUE.equals(jdbc.queryForObject("select exists(select 1 from pdv_configuracoes c join pdv_terminais p on p.tenant_id=c.tenant_id and p.id=c.terminal_id where c.tenant_id=? and c.filial_id=? and p.ativo=true)",Boolean.class,t,filialId)));
  return result;
 }
 @PutMapping("/empresa") @PreAuthorize("hasAuthority('RBAC_GERENCIAR')") @Transactional
 public Map<String,Object> salvarEmpresa(@Valid @RequestBody EmpresaRequest r) {
  UUID t=context.tenantId();
  jdbc.update("update empresas set razao_social=?,nome_fantasia=?,atualizado_em=now() where tenant_id=? and id=(select id from empresas where tenant_id=? order by criado_em,id limit 1)",r.razaoSocial().trim(),r.nomeFantasia().trim(),t,t);
  // The document and trial contact remain the original verified registration data.
  jdbc.update("""
   insert into onboarding_configuracoes(tenant_id,cep,endereco,numero,complemento,bairro,cidade,uf,empresa_revisada)
   values (?,?,?,?,?,?,?,?,true) on conflict(tenant_id) do update set cep=excluded.cep,
   endereco=excluded.endereco,numero=excluded.numero,complemento=excluded.complemento,bairro=excluded.bairro,
   cidade=excluded.cidade,uf=excluded.uf,empresa_revisada=true,atualizado_em=now()
   """,t,r.cep(),r.endereco().trim(),r.numero().trim(),r.complemento(),r.bairro().trim(),r.cidade().trim(),r.uf());
  var docs=jdbc.query("select cnpj from empresas where tenant_id=? order by criado_em,id limit 1",(rs,n)->rs.getString(1),t);
  UUID filialId=onboarding.prepararFilial(r.nomeFilial(),docs.stream().findFirst().orElse(null));
  jdbc.update("update filiais set nome=?,atualizado_em=now() where tenant_id=? and id=?",r.nomeFilial().trim(),t,filialId);
  return dados();
 }
 @PutMapping("/equipe-revisada") @PreAuthorize("hasAuthority('RBAC_GERENCIAR')")
 public Map<String,Object> revisarEquipe() {
  jdbc.update("insert into onboarding_configuracoes(tenant_id,equipe_revisada) values (?,true) on conflict(tenant_id) do update set equipe_revisada=true,atualizado_em=now()",context.tenantId());
  return dados();
 }
 @PostMapping("/adiar") @PreAuthorize("hasAuthority('RBAC_GERENCIAR')") @Transactional
 public void adiar() {
  var docs=jdbc.query("select cnpj from empresas where tenant_id=? order by criado_em,id limit 1",(rs,n)->rs.getString(1),context.tenantId());
  onboarding.prepararFilial("Matriz",docs.stream().findFirst().orElse(null));
 }
 public record EmpresaRequest(
  @NotBlank @Size(max=200) String razaoSocial, @NotBlank @Size(max=200) String nomeFantasia,
  @NotBlank @Pattern(regexp="[0-9]{5}-?[0-9]{3}") String cep,
  @NotBlank @Size(max=200) String endereco, @NotBlank @Size(max=20) String numero,
  @Size(max=100) String complemento, @NotBlank @Size(max=100) String bairro,
  @NotBlank @Size(max=100) String cidade, @NotBlank @Pattern(regexp="AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO") String uf,
  @NotBlank @Size(max=200) String nomeFilial) {}
}
