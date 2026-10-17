package com.traxup.tplug.erp.saas;
import com.traxup.tplug.erp.auth.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
@Service
@Transactional
public class SaasService {
 private final JdbcTemplate db; private final TenantContext context;
 public SaasService(JdbcTemplate db,TenantContext context){this.db=db;this.context=context;}
 private static final ZoneId ZONE=ZoneId.of("America/Recife");
 private LocalDate hoje(){return LocalDate.now(ZONE);}
 private ResponseStatusException erro(String m){return new ResponseStatusException(HttpStatus.CONFLICT,m);}
 private Map<String,Object> one(String sql,Object...args){var rows=db.queryForList(sql,args);if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Registro não encontrado");return rows.getFirst();}
 private void evento(UUID trial,Long fatura,String acao,String texto){db.update("INSERT INTO saas_eventos(trial_id,fatura_id,operador_id,acao,detalhe) VALUES(?,?,?,?,?)",trial,fatura,context.usuarioIdOuNulo(),acao,texto);}
 private Map<String,Object> trial(UUID id){return one("SELECT * FROM trials_saas WHERE id=? FOR UPDATE",id);}
 private Map<String,Object> assinatura(UUID id){return one("SELECT * FROM saas_assinaturas WHERE trial_id=? FOR UPDATE",id);}
 private BigDecimal preco(String plano){return (BigDecimal)one("SELECT preco FROM saas_planos WHERE nome=?",plano).get("preco");}
 private BigDecimal precoContrato(SaasController.Contrato d){BigDecimal valor=preco(d.plano());if("Corporativo".equals(d.plano())){if(d.mensalidade()==null)throw erro("Informe o valor mensal negociado do contrato Corporativo");return d.mensalidade();}if(valor.signum()<=0)throw erro("Plano sem preço comercial definido");return valor;}
 @Transactional(readOnly=true)
 public Map<String,Object> painel(){
  var eventos=db.queryForList("SELECT trial_id,fatura_id,acao,detalhe,criado_em FROM saas_eventos ORDER BY id");
  var clientes=db.query("""
   SELECT t.*,e.nome_fantasia,e.razao_social,u.nome responsavel,a.plano,a.mensalidade,a.modulos,a.vencimento,a.bloqueada,a.inicio inicio_assinatura,
    EXISTS(SELECT 1 FROM saas_faturas f WHERE f.trial_id=t.id AND NOT f.cancelada AND f.pago<f.valor AND f.vencimento<(CURRENT_TIMESTAMP AT TIME ZONE 'America/Recife')::date) inadimplente,
    (SELECT count(*) FROM usuarios x WHERE x.tenant_id=t.tenant_id AND x.ativo) usuarios
   FROM trials_saas t JOIN empresas e ON e.id=t.empresa_id JOIN usuarios u ON u.id=t.administrador_id
   LEFT JOIN saas_assinaturas a ON a.trial_id=t.id ORDER BY t.criado_em DESC
   """,(rs,n)->{
    Map<String,Object> c=new LinkedHashMap<>();UUID id=rs.getObject("id",UUID.class);boolean contrato=rs.getString("plano")!=null;
    c.put("id",id.toString());c.put("empresa",Optional.ofNullable(rs.getString("nome_fantasia")).orElse(rs.getString("razao_social")));c.put("cnpj",rs.getString("documento"));
    c.put("responsavel",rs.getString("responsavel"));c.put("email",rs.getString("email"));c.put("telefone",rs.getString("telefone"));c.put("segmento",Optional.ofNullable(rs.getString("segmento")).orElse(""));
    c.put("plano",contrato?rs.getString("plano"):"Completo");c.put("mensalidade",contrato?rs.getBigDecimal("mensalidade"):BigDecimal.ZERO);
    c.put("modulos",contrato?List.of(rs.getString("modulos").split(",")):List.of("ERP","PDV","Fiscal","CRM","BI"));
    String fim=rs.getTimestamp("expira_em").toInstant().atZone(ZONE).toLocalDate().toString();
    c.put("trial",fim);c.put("inicio",contrato?rs.getDate("inicio_assinatura").toString():rs.getTimestamp("inicio_em").toInstant().atZone(ZONE).toLocalDate().toString());c.put("ativacao",rs.getTimestamp("admin_ativado_em")==null?null:rs.getTimestamp("admin_ativado_em").toInstant().atZone(ZONE).toLocalDate().toString());c.put("vencimento",contrato?rs.getDate("vencimento").toString():fim);
    c.put("situacao",contrato?(rs.getBoolean("bloqueada")?"Bloqueado":rs.getBoolean("inadimplente")?"Inadimplente":"Ativo"):"CANCELADO".equals(rs.getString("status"))?"Trial encerrado":"Trial");
    c.put("origemTrial",true);c.put("etapa",rs.getTimestamp("admin_ativado_em")==null?"Aguardando ativação":"Trial ativo");c.put("onboarding",rs.getString("onboarding_status"));
    c.put("lojas",rs.getInt("quantidade_lojas"));c.put("usuarios",rs.getInt("usuarios"));c.put("terminais",null);c.put("certificado",null);c.put("ultimo",null);
    var logs=eventos.stream().filter(e->id.equals(e.get("trial_id"))).map(e->e.get("criado_em")+" · "+e.get("detalhe")).toList();
    c.put("logs",logs);c.put("notas",eventos.stream().filter(e->id.equals(e.get("trial_id"))&&"NOTA".equals(e.get("acao"))).map(e->e.get("detalhe")).toList());c.put("faturas",List.of());return c;
   });
  var faturas=db.query("SELECT * FROM saas_faturas ORDER BY id DESC",(rs,n)->{
   Map<String,Object> f=new LinkedHashMap<>();long id=rs.getLong("id");f.put("id",id);f.put("cliente",rs.getString("trial_id"));f.put("numero","TRX-"+id);f.put("competencia",rs.getString("competencia"));f.put("emissao",rs.getDate("emissao").toString());f.put("vencimento",rs.getDate("vencimento").toString());f.put("valor",rs.getBigDecimal("valor"));f.put("pago",rs.getBigDecimal("pago"));f.put("forma",rs.getString("forma"));f.put("data",rs.getDate("data")==null?"":rs.getDate("data").toString());f.put("cancelada",rs.getBoolean("cancelada"));f.put("logs",eventos.stream().filter(e->Objects.equals(id,e.get("fatura_id"))).map(e->e.get("criado_em")+" · "+e.get("detalhe")).toList());return f;
  });
  var alteracoes=eventos.stream().filter(e->List.of("UPGRADE","DOWNGRADE").contains(e.get("acao"))).map(e->Map.of("tipo","UPGRADE".equals(e.get("acao"))?"Upgrade":"Downgrade","texto",clientes.stream().filter(c->String.valueOf(e.get("trial_id")).equals(c.get("id"))).map(c->String.valueOf(c.get("empresa"))).findFirst().orElse("Empresa")+" · "+e.get("detalhe"))).toList();
  return Map.of("alteracoes",alteracoes,"clientes",clientes,"faturas",faturas,"catalogo",db.queryForList("SELECT * FROM saas_planos ORDER BY CASE nome WHEN 'Pessoal' THEN 1 WHEN 'Básico' THEN 2 WHEN 'Essencial' THEN 3 WHEN 'Completo' THEN 4 ELSE 5 END"),"relatorios",db.queryForList("SELECT id,nome,plano,periodo FROM saas_relatorios WHERE operador_id=? ORDER BY id",context.usuarioIdOuNulo()),"hoje",hoje().toString());
 }
 public void prorrogar(UUID id,int dias){var t=trial(id);if(!List.of("ATIVO","EXPIRADO").contains(t.get("status")))throw erro("Trial encerrado ou convertido");db.update("UPDATE trials_saas SET expira_em=GREATEST(expira_em,NOW()) + (? * INTERVAL '1 day'),status='ATIVO',atualizado_em=NOW() WHERE id=?",dias,id);evento(id,null,"PRORROGACAO","Trial prorrogado por "+dias+" dias");}
 public void converter(UUID id,SaasController.Contrato d){var t=trial(id);if(!List.of("ATIVO","EXPIRADO").contains(t.get("status")))throw erro("Trial já convertido ou encerrado");if(t.get("admin_ativado_em")==null)throw erro("O responsável precisa ativar sua conta antes da contratação");BigDecimal valor=precoContrato(d);db.update("INSERT INTO saas_assinaturas(trial_id,plano,mensalidade,modulos,inicio,vencimento) VALUES(?,?,?,?,?,?)",id,d.plano(),valor,String.join(",",new LinkedHashSet<>(d.modulos())),hoje(),hoje().plusMonths(1));db.update("UPDATE trials_saas SET status='CONVERTIDO',atualizado_em=NOW() WHERE id=?",id);evento(id,null,"CONVERSAO","Assinatura contratada: "+d.plano());emitirInterno(id,valor,hoje().plusMonths(1));}
 public void encerrar(UUID id){var t=trial(id);if(!List.of("ATIVO","EXPIRADO").contains(t.get("status")))throw erro("Trial não está em avaliação");db.update("UPDATE trials_saas SET status='CANCELADO',atualizado_em=NOW() WHERE id=?",id);db.update("UPDATE trial_activation_emails SET status='CANCELLED' WHERE trial_id=?",id);evento(id,null,"ENCERRAMENTO","Trial encerrado");}
 public void plano(UUID id,SaasController.Contrato d){var a=assinatura(id);var valor=precoContrato(d);db.update("UPDATE saas_assinaturas SET plano=?,mensalidade=?,modulos=? WHERE trial_id=?",d.plano(),valor,String.join(",",new LinkedHashSet<>(d.modulos())),id);evento(id,null,valor.compareTo((BigDecimal)a.get("mensalidade"))>0?"UPGRADE":valor.compareTo((BigDecimal)a.get("mensalidade"))<0?"DOWNGRADE":"ALTERACAO","Plano alterado para "+d.plano()+"; faturas emitidas preservadas");}
 public void bloqueio(UUID id,boolean bloqueado){assinatura(id);var t=trial(id);if(Objects.equals(t.get("tenant_id"),context.tenantId()))throw erro("Não é permitido bloquear o tenant do operador");db.update("UPDATE saas_assinaturas SET bloqueada=? WHERE trial_id=?",bloqueado,id);db.update("UPDATE tenants SET ativo=?,atualizado_em=NOW() WHERE id=?",!bloqueado,t.get("tenant_id"));evento(id,null,"BLOQUEIO",bloqueado?"Acesso bloqueado":"Acesso liberado");}
 public void nota(UUID id,String texto){trial(id);evento(id,null,"NOTA",texto.trim());}
 private void emitirInterno(UUID id,BigDecimal valor,LocalDate vencimento){Long f=db.queryForObject("INSERT INTO saas_faturas(trial_id,competencia,emissao,vencimento,valor) VALUES(?,?,?,?,?) RETURNING id",Long.class,id,YearMonth.from(hoje()).toString(),hoje(),vencimento,valor);evento(id,f,"EMISSAO","Fatura emitida: R$ "+valor);}
 public void emitir(SaasController.Emissao d){assinatura(d.cliente());var es=db.queryForList("SELECT trial_id,valor,vencimento FROM saas_faturas WHERE chave=?",d.chave());if(!es.isEmpty()){var e=es.getFirst();if(!e.get("trial_id").equals(d.cliente())||((BigDecimal)e.get("valor")).compareTo(d.valor())!=0||!e.get("vencimento").toString().equals(d.vencimento().toString()))throw erro("Chave de emissão já utilizada");return;}Long id=db.queryForObject("INSERT INTO saas_faturas(trial_id,competencia,emissao,vencimento,valor,chave) VALUES(?,?,?,?,?,?) RETURNING id",Long.class,d.cliente(),YearMonth.from(hoje()).toString(),hoje(),d.vencimento(),d.valor(),d.chave());evento(d.cliente(),id,"EMISSAO","Fatura emitida: R$ "+d.valor());}
 private Map<String,Object> fatura(long id){return one("SELECT * FROM saas_faturas WHERE id=? FOR UPDATE",id);}
 public void pagamento(long id,SaasController.Pagamento d){var f=fatura(id);var existentes=db.queryForList("SELECT fatura_id,valor,data,forma FROM saas_pagamentos WHERE chave=?",d.chave());if(!existentes.isEmpty()){var e=existentes.getFirst();if(!Objects.equals(e.get("fatura_id"),id)||((BigDecimal)e.get("valor")).compareTo(d.valor())!=0||!e.get("data").toString().equals(d.data().toString())||!e.get("forma").equals(d.forma()))throw erro("Chave de pagamento já utilizada");return;}if(Boolean.TRUE.equals(f.get("cancelada")))throw erro("Fatura cancelada");BigDecimal saldo=((BigDecimal)f.get("valor")).subtract((BigDecimal)f.get("pago"));if(d.valor().compareTo(saldo)>0)throw erro("Pagamento maior que o saldo");db.update("INSERT INTO saas_pagamentos(fatura_id,valor,forma,data,chave,operador_id) VALUES(?,?,?,?,?,?)",id,d.valor(),d.forma(),d.data(),d.chave(),context.usuarioIdOuNulo());db.update("UPDATE saas_faturas SET pago=pago+?,forma=?,data=? WHERE id=?",d.valor(),d.forma(),d.data(),id);evento((UUID)f.get("trial_id"),id,"PAGAMENTO","Recebimento manual: R$ "+d.valor()+" em "+d.data());}
 public void vencimento(long id,LocalDate data){var f=fatura(id);if(Boolean.TRUE.equals(f.get("cancelada"))||((BigDecimal)f.get("pago")).compareTo((BigDecimal)f.get("valor"))>=0)throw erro("Fatura encerrada");db.update("UPDATE saas_faturas SET vencimento=? WHERE id=?",data,id);evento((UUID)f.get("trial_id"),id,"VENCIMENTO","Vencimento alterado para "+data);}
 public void cancelar(long id){var f=fatura(id);if(((BigDecimal)f.get("pago")).signum()>0)throw erro("Não é possível cancelar fatura com recebimentos");if(Boolean.TRUE.equals(f.get("cancelada")))return;db.update("UPDATE saas_faturas SET cancelada=TRUE WHERE id=?",id);evento((UUID)f.get("trial_id"),id,"CANCELAMENTO","Fatura cancelada");}
 public void catalogo(String nome,SaasController.Plano d){preco(nome);db.update("UPDATE saas_planos SET preco=?,usuarios=?,lojas=? WHERE nome=?",d.preco(),d.usuarios(),d.lojas(),nome);evento(null,null,"CATALOGO","Catálogo alterado: "+nome+"; contratos preservados");}
 public void relatorio(SaasController.Relatorio d){if(d.periodo()!=3&&d.periodo()!=6)throw erro("Período inválido");if(!d.plano().isBlank())preco(d.plano());db.update("INSERT INTO saas_relatorios(operador_id,nome,plano,periodo) VALUES(?,?,?,?) ON CONFLICT(operador_id,nome) DO UPDATE SET plano=EXCLUDED.plano,periodo=EXCLUDED.periodo",context.usuarioIdOuNulo(),d.nome().trim(),d.plano(),d.periodo());}
}
