package com.traxup.tplug.erp.onboarding;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.empresa.*;
import com.traxup.tplug.erp.filial.*;
import com.traxup.tplug.erp.trial.TrialSaasRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class OnboardingService {
 private final TenantContext context; private final EmpresaRepository empresas; private final FilialRepository filiais; private final TrialSaasRepository trials; private final JdbcTemplate jdbc;
 public OnboardingService(TenantContext context,EmpresaRepository empresas,FilialRepository filiais,TrialSaasRepository trials,JdbcTemplate jdbc){this.context=context;this.empresas=empresas;this.filiais=filiais;this.trials=trials;this.jdbc=jdbc;}
 public Status status(){UUID t=context.tenantId();var es=empresas.findAllByTenantId(t);var fs=filiais.findAllByTenantId(t);boolean concluido=jdbc.queryForObject("select exists(select 1 from trials_saas where tenant_id=? and onboarding_status='CONCLUIDO')",Boolean.class,t);return new Status(concluido,!es.isEmpty(),!fs.isEmpty());}
 @Transactional public Status concluir(String nomeFilial,String cnpj){UUID t=context.tenantId();UUID u=context.usuarioIdOuNulo();Empresa e=empresas.findAllByTenantId(t).stream().findFirst().orElseThrow();Filial f=filiais.findAllByTenantIdAndEmpresaId(t,e.getId()).stream().findFirst().orElseGet(()->filiais.save(new Filial(e.getTenant(),e,nomeFilial==null||nomeFilial.isBlank()?"Matriz":nomeFilial.trim(),digitos(cnpj))));
   if(u!=null) jdbc.update("insert into usuario_filiais (tenant_id,usuario_id,filial_id) values (?,?,?) on conflict do nothing",t,u,f.getId());
   jdbc.update("update trials_saas set onboarding_status='CONCLUIDO', onboarding_concluido_em=now(), atualizado_em=now() where tenant_id=? and onboarding_status<>'CONCLUIDO'",t);
   return new Status(true,true,true);
 }
 private static String digitos(String v){return v==null?null:v.replaceAll("\\D","");}
 public record Status(boolean concluido,boolean empresaConfigurada,boolean filialConfigurada){}
}
