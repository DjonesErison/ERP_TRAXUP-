package com.traxup.tplug.erp.saas;
import com.traxup.tplug.erp.tenant.*;
import com.traxup.tplug.erp.empresa.*;
import com.traxup.tplug.erp.usuario.*;
import com.traxup.tplug.erp.trial.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SaasIntegrationTest {
 @Autowired MockMvc mvc; @Autowired JdbcTemplate db; @Autowired TenantRepository tenants;
 @Autowired UsuarioApplicationService usuarios; @Autowired EmpresaRepository empresas;
 @Autowired TrialSaasRepository trials; @Autowired EntityManager em;
 private Usuario operador(boolean autorizar){var tenant=tenants.saveAndFlush(new Tenant("Operação TRAXUP"));var user=usuarios.criar(tenant.getId(),"Operador","op-"+UUID.randomUUID()+"@example.test","SenhaForte123!");em.flush();if(autorizar)db.update("INSERT INTO saas_operadores(usuario_id) VALUES(?)",user.getId());return user;}
 private RequestPostProcessor token(Usuario u){return jwt().jwt(t->t.subject(u.getId().toString()).claim("tenant_id",u.getTenant().getId().toString())).authorities(new SimpleGrantedAuthority("RBAC_GERENCIAR"));}
 private TrialSaas trial(){var tenant=tenants.saveAndFlush(new Tenant("Cliente SaaS"));var empresa=empresas.save(new Empresa(tenant,"Razão social","Cliente SaaS","12345678000190"));var user=usuarios.criar(tenant.getId(),"Responsável","cliente-"+UUID.randomUUID()+"@example.test","SenhaForte123!");var t=new TrialSaas(tenant,empresa,user,user.getEmail(),"12345678000190","81999999999","Varejo",1,"v1",UUID.randomUUID().toString(),Instant.now());t.marcarAdminAtivado(Instant.now());trials.saveAndFlush(t);return t;}
 private void converter(Usuario op,TrialSaas t)throws Exception{mvc.perform(post("/api/v1/saas/trials/"+t.getId()+"/converter").with(token(op)).contentType(MediaType.APPLICATION_JSON).content("{\"plano\":\"Essencial\",\"modulos\":[\"ERP\",\"PDV\"]}")).andExpect(status().isOk());}
 @Test void exigeOperadorGlobalERevogaImediatamente()throws Exception{
  mvc.perform(get("/api/v1/saas/painel")).andExpect(status().isUnauthorized());
  var op=operador(false);mvc.perform(get("/api/v1/saas/acesso").with(token(op))).andExpect(status().isOk()).andExpect(jsonPath("$.permitido").value(false));mvc.perform(get("/api/v1/saas/painel").with(token(op))).andExpect(status().isForbidden());
  db.update("INSERT INTO saas_operadores(usuario_id) VALUES(?)",op.getId());
  mvc.perform(get("/api/v1/saas/painel").with(token(op))).andExpect(status().isOk()).andExpect(jsonPath("$.catalogo.length()").value(5));
  mvc.perform(get("/api/v1/saas/painel").with(jwt().jwt(t->t.subject(op.getId().toString()).claim("tenant_id",UUID.randomUUID().toString())))).andExpect(status().isForbidden());
  db.update("UPDATE saas_operadores SET ativo=FALSE WHERE usuario_id=?",op.getId());mvc.perform(get("/api/v1/saas/painel").with(token(op))).andExpect(status().isForbidden());
 }
 @Test void converteUmaVezEPersisteRecebimentoIdempotente()throws Exception{
  var op=operador(true);var t=trial();converter(op,t);
  assertEquals(1,db.queryForObject("SELECT count(*) FROM saas_assinaturas WHERE trial_id=?",Integer.class,t.getId()));
  converterConflito(op,t);
  long id=db.queryForObject("SELECT id FROM saas_faturas WHERE trial_id=?",Long.class,t.getId());
  String chave=UUID.randomUUID().toString();String body="{\"valor\":10.00,\"forma\":\"Pix\",\"data\":\"2026-01-01\",\"chave\":\""+chave+"\"}";
  for(int i=0;i<2;i++)mvc.perform(post("/api/v1/saas/faturas/"+id+"/pagamentos").with(token(op)).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
  assertEquals(0,new java.math.BigDecimal("10.00").compareTo(db.queryForObject("SELECT pago FROM saas_faturas WHERE id=?",java.math.BigDecimal.class,id)));
  assertEquals(1,db.queryForObject("SELECT count(*) FROM saas_pagamentos WHERE fatura_id=?",Integer.class,id));
  mvc.perform(post("/api/v1/saas/faturas/"+id+"/pagamentos").with(token(op)).contentType(MediaType.APPLICATION_JSON).content(body.replace("10.00","999.00").replace(chave,UUID.randomUUID().toString()))).andExpect(status().isConflict());
  mvc.perform(post("/api/v1/saas/faturas/"+id+"/cancelar").with(token(op))).andExpect(status().isConflict());
  mvc.perform(get("/api/v1/saas/painel").with(token(op))).andExpect(status().isOk()).andExpect(jsonPath("$.clientes[?(@.id == '"+t.getId()+"')].plano").value("Essencial"));
 }
 private void converterConflito(Usuario op,TrialSaas t)throws Exception{mvc.perform(post("/api/v1/saas/trials/"+t.getId()+"/converter").with(token(op)).contentType(MediaType.APPLICATION_JSON).content("{\"plano\":\"Essencial\",\"modulos\":[\"ERP\"]}")).andExpect(status().isConflict());}
 @Test void validaDiasModulosECatalogoSemReajustarContrato()throws Exception{
  var op=operador(true);var t=trial();
  mvc.perform(post("/api/v1/saas/trials/"+t.getId()+"/prorrogar").with(token(op)).contentType(MediaType.APPLICATION_JSON).content("{\"dias\":31}")).andExpect(status().isBadRequest());
  mvc.perform(post("/api/v1/saas/trials/"+t.getId()+"/converter").with(token(op)).contentType(MediaType.APPLICATION_JSON).content("{\"plano\":\"Essencial\",\"modulos\":[\"INVALIDO\"]}")).andExpect(status().isBadRequest());
  converter(op,t);
  mvc.perform(put("/api/v1/saas/planos/Essencial").with(token(op)).contentType(MediaType.APPLICATION_JSON).content("{\"preco\":199.90,\"usuarios\":10,\"lojas\":2}")).andExpect(status().isOk());
  assertEquals(0,new java.math.BigDecimal("149.90").compareTo(db.queryForObject("SELECT mensalidade FROM saas_assinaturas WHERE trial_id=?",java.math.BigDecimal.class,t.getId())));
 }
 @Test void inadimplenciaVemDoSaldoDasFaturas()throws Exception{
  var op=operador(true);var t=trial();converter(op,t);
  db.update("UPDATE saas_faturas SET vencimento=DATE '2000-01-01' WHERE trial_id=?",t.getId());
  mvc.perform(get("/api/v1/saas/painel").with(token(op))).andExpect(status().isOk()).andExpect(jsonPath("$.clientes[?(@.id == '"+t.getId()+"')].situacao").value("Inadimplente"));
 }
 @Test void bloqueioInvalidaJwtExistenteEImpedeAcessoDoCliente()throws Exception{
  var op=operador(true);var t=trial();converter(op,t);
  mvc.perform(post("/api/v1/saas/clientes/"+t.getId()+"/bloqueio").with(token(op)).contentType(MediaType.APPLICATION_JSON).content("{\"bloqueado\":true}")).andExpect(status().isOk());
  mvc.perform(get("/api/v1/rbac/perfis").with(token(t.getAdministrador()))).andExpect(status().isForbidden());
  mvc.perform(post("/api/v1/saas/clientes/"+t.getId()+"/bloqueio").with(token(op)).contentType(MediaType.APPLICATION_JSON).content("{\"bloqueado\":false}")).andExpect(status().isOk());
  mvc.perform(get("/api/v1/saas/painel").with(token(t.getAdministrador()))).andExpect(status().isForbidden());
 }
 @Test void emissaoRepetidaNaoDuplicaFatura()throws Exception{
  var op=operador(true);var t=trial();converter(op,t);String chave=UUID.randomUUID().toString();
  String body="{\"cliente\":\""+t.getId()+"\",\"valor\":50.00,\"vencimento\":\"2026-12-31\",\"chave\":\""+chave+"\"}";
  for(int i=0;i<2;i++)mvc.perform(post("/api/v1/saas/faturas").with(token(op)).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
  assertEquals(2,db.queryForObject("SELECT count(*) FROM saas_faturas WHERE trial_id=?",Integer.class,t.getId()));
 }
 @Test void corporativoExigeValorNegociado()throws Exception{
  var op=operador(true);var t=trial();
  var url="/api/v1/saas/trials/"+t.getId()+"/converter";
  mvc.perform(post(url).with(token(op)).contentType(MediaType.APPLICATION_JSON).content("{\"plano\":\"Corporativo\",\"modulos\":[\"ERP\"]}")).andExpect(status().isConflict());
  mvc.perform(post(url).with(token(op)).contentType(MediaType.APPLICATION_JSON).content("{\"plano\":\"Corporativo\",\"modulos\":[\"ERP\"],\"mensalidade\":700.00}")).andExpect(status().isOk());
  assertEquals(0,new java.math.BigDecimal("700.00").compareTo(db.queryForObject("SELECT mensalidade FROM saas_assinaturas WHERE trial_id=?",java.math.BigDecimal.class,t.getId())));
 }
 @Test void relatoriosSaoPrivadosDoOperador()throws Exception{
  var a=operador(true);var b=operador(true);
  mvc.perform(post("/api/v1/saas/relatorios").with(token(a)).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Mensal A\",\"plano\":\"\",\"periodo\":6}")).andExpect(status().isOk());
  mvc.perform(get("/api/v1/saas/painel").with(token(b))).andExpect(status().isOk()).andExpect(jsonPath("$.relatorios.length()").value(0));
 }
}
