package com.traxup.tplug.erp.onboarding;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.empresa.*;
import com.traxup.tplug.erp.filial.*;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.trial.TrialSaasRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OnboardingServiceTest {
 @Test void concluirCriaPrimeiraFilialEVinculaAdministrador(){
  UUID tenantId=UUID.randomUUID(), usuarioId=UUID.randomUUID();
  TenantContext context=mock(TenantContext.class); EmpresaRepository empresas=mock(EmpresaRepository.class);
  FilialRepository filiais=mock(FilialRepository.class); TrialSaasRepository trials=mock(TrialSaasRepository.class); JdbcTemplate jdbc=mock(JdbcTemplate.class);
  Tenant tenant=new Tenant("Demo"); Empresa empresa=new Empresa(tenant,"Demo LTDA","Demo","12345678000199");
  when(context.tenantId()).thenReturn(tenantId); when(context.usuarioIdOuNulo()).thenReturn(usuarioId);
  when(empresas.findAllByTenantId(tenantId)).thenReturn(List.of(empresa)); when(filiais.findAllByTenantIdAndEmpresaId(eq(tenantId),any())).thenReturn(List.of());
  when(filiais.save(any())).thenAnswer(i->i.getArgument(0));
  var service=new OnboardingService(context,empresas,filiais,trials,jdbc);
  var status=service.concluir("Matriz","12.345.678/0001-99");
  assertThat(status.concluido()).isTrue(); assertThat(status.filialConfigurada()).isTrue();
  verify(filiais).save(any(Filial.class)); verify(jdbc).update(startsWith("insert into usuario_filiais"),eq(tenantId),eq(usuarioId),any());
  verify(jdbc).update(startsWith("update trials_saas"),eq(tenantId));
 }
}
