package com.traxup.tplug.erp.saas;
import com.traxup.tplug.erp.auth.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
@Component("saasAccess")
public class SaasAccess {
 private final JdbcTemplate db; private final TenantContext context;
 public SaasAccess(JdbcTemplate db,TenantContext context){this.db=db;this.context=context;}
 public boolean permitido(){
  var user=context.usuarioIdOuNulo(); if(user==null)return false;
  return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM saas_operadores o JOIN usuarios u ON u.id=o.usuario_id JOIN tenants t ON t.id=u.tenant_id WHERE o.usuario_id=? AND o.ativo AND u.ativo AND t.ativo AND u.tenant_id=?)",Boolean.class,user,context.tenantId()));
 }
}
