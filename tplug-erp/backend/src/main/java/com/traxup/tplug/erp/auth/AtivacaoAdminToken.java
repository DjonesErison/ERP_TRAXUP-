package com.traxup.tplug.erp.auth;

import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.usuario.Usuario;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="ativacao_admin_tokens")
public class AtivacaoAdminToken {
 @Id private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="tenant_id",nullable=false) private Tenant tenant;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="usuario_id",nullable=false) private Usuario usuario;
 @Column(name="token_hash",nullable=false,unique=true,length=64) private String tokenHash;
 @Column(name="expira_em",nullable=false) private Instant expiraEm;
 @Column(name="usado_em") private Instant usadoEm;
 @Column(name="criado_em",nullable=false) private Instant criadoEm;
 protected AtivacaoAdminToken(){}
 public AtivacaoAdminToken(Tenant tenant,Usuario usuario,String tokenHash,Instant expiraEm){this.id=UUID.randomUUID();this.tenant=tenant;this.usuario=usuario;this.tokenHash=tokenHash;this.expiraEm=expiraEm;this.criadoEm=Instant.now();}
 public Usuario getUsuario(){return usuario;} public boolean podeUsar(Instant agora){return usadoEm==null&&expiraEm.isAfter(agora);} public void marcarUsado(Instant agora){usadoEm=agora;}
}
