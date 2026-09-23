package com.traxup.tplug.erp.trial;

import com.traxup.tplug.erp.auth.*;
import com.traxup.tplug.erp.trial.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties={"trial.mail.enabled=true","trial.mail.initial-delay-ms=3600000"})
class TrialConcurrencyIntegrationTest {
    @Autowired TrialProvisioningService service;
    @Autowired AuthApplicationService auth;
    @Autowired AccessRecoveryQueue queue;
    @Autowired JdbcTemplate jdbc;
    @Test void mesmoDocumentoConcorrenteCriaUmTenantETokenSoPodeSerConsumidoUmaVez() throws Exception {
        String doc="98765432109";
        long before=jdbc.queryForObject("SELECT count(*) FROM tenants",Long.class);
        var start=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(2)) {
            Callable<Boolean> signup=()->{start.await();try {
                service.provisionar(new TrialCadastroRequest("Ana","Concurrent","Concurrent",doc,"87999999999","race@example.test",null,1,true,"v1",UUID.randomUUID().toString()));return true;
            } catch(org.springframework.dao.DataIntegrityViolationException | com.traxup.tplug.erp.shared.exception.RecursoConflitanteException e) {return false;}};
            var a=pool.submit(signup);var b=pool.submit(signup);start.countDown();
            assertThat(List.of(a.get(30,TimeUnit.SECONDS),b.get(30,TimeUnit.SECONDS))).containsExactlyInAnyOrder(true,false);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM tenants",Long.class)).isEqualTo(before+1);
            jdbc.update("UPDATE trials_saas SET admin_ativado_em=NOW() WHERE documento=?",doc);
            queue.requestDocument(doc,"race@example.test");var delivery=queue.prepare().orElseThrow();queue.sent(delivery);
            assertThat(delivery.action()).isEqualTo("RECOVER");
            var recoverStart=new CountDownLatch(1);
            Callable<Boolean> recover=()->{recoverStart.await();try {auth.confirmarRecuperacao(delivery.token(),"SenhaNova123!");return true;}catch(com.traxup.tplug.erp.shared.exception.AutenticacaoException e){return false;}};
            var c=pool.submit(recover);var d=pool.submit(recover);recoverStart.countDown();
            assertThat(List.of(c.get(30,TimeUnit.SECONDS),d.get(30,TimeUnit.SECONDS))).containsExactlyInAnyOrder(true,false);
        } finally {
            var ids=jdbc.queryForList("SELECT tenant_id FROM trials_saas WHERE documento=?",UUID.class,doc);
            for(UUID id:ids) {
                jdbc.update("DELETE FROM access_recovery_emails WHERE usuario_id IN (SELECT id FROM usuarios WHERE tenant_id=?)",id);
                jdbc.update("DELETE FROM trial_activation_emails WHERE trial_id IN (SELECT id FROM trials_saas WHERE tenant_id=?)",id);
                for(String table:List.of("recuperacao_senha_tokens","ativacao_admin_tokens","refresh_tokens","trials_saas","usuario_perfis","perfil_permissoes","perfis","usuarios","empresas")) jdbc.update("DELETE FROM "+table+" WHERE tenant_id=?",id);
                jdbc.update("DELETE FROM tenants WHERE id=?",id);
            }
        }
    }
}
