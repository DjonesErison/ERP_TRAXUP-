package com.traxup.tplug.erp.saas;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
/** Revalida bloqueios persistidos também para JWTs emitidos antes da suspensão. */
public class SaasAccessFilter extends OncePerRequestFilter {
 private final JdbcTemplate db;
 public SaasAccessFilter(JdbcTemplate db){this.db=db;}
 @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
  var auth=SecurityContextHolder.getContext().getAuthentication();
  if(request.getRequestURI().startsWith("/api/") && auth instanceof JwtAuthenticationToken jwt){
   try{
    String claim=jwt.getToken().getClaimAsString("tenant_id");
    UUID tenant=claim==null?null:UUID.fromString(claim);
    boolean bloqueado=Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM trials_saas t LEFT JOIN saas_assinaturas a ON a.trial_id=t.id WHERE t.tenant_id=? AND (a.bloqueada=TRUE OR t.status='CANCELADO'))",Boolean.class,tenant));
    if(bloqueado){response.setStatus(403);response.setContentType("application/problem+json");response.getWriter().write("{\"status\":403,\"detail\":\"Acesso da empresa suspenso. Contate o suporte.\"}");return;}
   }catch(IllegalArgumentException ignored){/* TenantContext valida tokens sem identificação válida. */}
  }
  chain.doFilter(request,response);
 }
}
