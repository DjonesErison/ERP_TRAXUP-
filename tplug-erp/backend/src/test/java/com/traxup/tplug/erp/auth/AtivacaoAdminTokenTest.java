package com.traxup.tplug.erp.auth;

import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.usuario.Usuario;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

class AtivacaoAdminTokenTest {
 @Test void tokenEhUsoUnicoEExpira() {
   var tenant=new Tenant("Demo");
   var token=new AtivacaoAdminToken(tenant,new Usuario(tenant,"Admin","a@b.com","hash"),"hash",Instant.now().plusSeconds(60));
   var agora=Instant.now();
   assertThat(token.podeUsar(agora)).isTrue();
   token.marcarUsado(agora);
   assertThat(token.podeUsar(agora)).isFalse();
 }
 @Test void tokenExpiradoNaoPodeSerUsado() {
   var tenant=new Tenant("Demo");
   var token=new AtivacaoAdminToken(tenant,new Usuario(tenant,"Admin","a@b.com","hash"),"hash",Instant.now().minusSeconds(1));
   assertThat(token.podeUsar(Instant.now())).isFalse();
 }
}
