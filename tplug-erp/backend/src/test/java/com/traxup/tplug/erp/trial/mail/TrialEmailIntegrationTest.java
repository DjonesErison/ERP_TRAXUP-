package com.traxup.tplug.erp.trial.mail;

import com.traxup.tplug.erp.auth.AuthApplicationService;
import com.traxup.tplug.erp.trial.TrialProvisioningService;
import com.traxup.tplug.erp.trial.api.TrialCadastroRequest;
import com.traxup.tplug.erp.trial.api.TrialCadastroResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties={"trial.mail.enabled=true","trial.mail.initial-delay-ms=3600000","spring.mail.test-connection=false"})
@AutoConfigureMockMvc
@Transactional
class TrialEmailIntegrationTest {
    @Autowired TrialProvisioningService provisioning;
    @Autowired TrialEmailQueue queue;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired AuthApplicationService auth;
    @Autowired jakarta.persistence.EntityManager entityManager;
    TrialCadastroRequest request(String key) { return new TrialCadastroRequest("Ana", "Loja", "Loja LTDA", "12345678000199", "87999999999", "ana@example.test", "Varejo", 1, true, "2026-09", key); }
    TrialCadastroResponse create() { return provisioning.provisionar(request(UUID.randomUUID().toString())); }
    String state(UUID id) { return jdbc.queryForObject("select status from trial_activation_emails where trial_id=?",String.class,id); }
    void confirm(String token,int expected) throws Exception {
        mvc.perform(post("/api/v1/auth/ativacao-admin/confirmar").contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\""+token+"\",\"novaSenha\":\"SenhaTeste123!\"}")).andExpect(status().is(expected));
    }
    @Test void cadastroAtomicoSemTokenPublicoEIdempotente() {
        var req=request(UUID.randomUUID().toString());var first=provisioning.provisionar(req);var again=provisioning.provisionar(req);
        assertThat(first.ativacaoToken()).isNull();assertThat(first.proximoPasso()).isEqualTo("VERIFICAR_EMAIL");
        assertThat(again.trialId()).isEqualTo(first.trialId());
        assertThat(jdbc.queryForObject("select count(*) from trial_activation_emails where trial_id=?",Integer.class,first.trialId())).isEqualTo(1);
        assertThat(state(first.trialId())).isEqualTo("PENDING");
    }
    @Test void ativacaoPublicaPermiteLoginEInvalidaTodosOsLinks() throws Exception {
        var trial=create();var d=queue.prepare().orElseThrow();queue.sent(d);
        var userId=jdbc.queryForObject("select administrador_id from trials_saas where id=?",UUID.class,trial.trialId());
        jdbc.update("update trial_activation_emails set last_requested_at=NOW()-INTERVAL '2 minutes' where trial_id=?",trial.trialId());
        queue.resend(trial.tenantId(),"ANA@example.test");var second=queue.prepare().orElseThrow();
        assertThat(second.token()).isNotEqualTo(d.token());
        confirm(d.token(),204);
        assertThat(auth.login(trial.tenantId(),"ana@example.test","SenhaTeste123!").accessToken()).isNotBlank();
        assertThat(jdbc.queryForObject("select count(*) from ativacao_admin_tokens where usuario_id=? and usado_em is null",Integer.class,userId)).isZero();
        confirm(second.token(),401);
    }
    @Test void linkExpiradoNaoAtiva() throws Exception {
        create();var d=queue.prepare().orElseThrow();
        entityManager.flush();
        jdbc.update("update ativacao_admin_tokens set expira_em=NOW()-INTERVAL '1 minute'");
        entityManager.clear();
        confirm(d.token(),401);
    }
    @Test void reenvioNeutroComCooldownELimite() throws Exception {
        var t=create();var d=queue.prepare().orElseThrow();queue.sent(d);
        queue.resend(t.tenantId(),"ana@example.test");assertThat(state(t.trialId())).isEqualTo("SENT");
        jdbc.update("update trial_activation_emails set last_requested_at=NOW()-INTERVAL '2 minutes' where trial_id=?",t.trialId());
        queue.resend(t.tenantId(),"wrong@example.test");assertThat(state(t.trialId())).isEqualTo("SENT");
        queue.resend(t.tenantId(),"ana@example.test");assertThat(state(t.trialId())).isEqualTo("PENDING");
        jdbc.update("update trial_activation_emails set status='SENT', requests_in_window=6,last_requested_at=NOW()-INTERVAL '2 minutes' where trial_id=?",t.trialId());
        queue.resend(t.tenantId(),"ana@example.test");assertThat(state(t.trialId())).isEqualTo("SENT");
        mvc.perform(post("/api/public/trials/reenviar-ativacao").contentType(MediaType.APPLICATION_JSON).content("{\"tenantId\":\""+UUID.randomUUID()+"\",\"email\":\"unknown@example.test\"}")).andExpect(status().isAccepted());
    }
    @Test void falhaSobreviveERetomaAposLeaseExpirado() {
        var t=create();var first=queue.prepare().orElseThrow();queue.failed(first);
        assertThat(state(t.trialId())).isEqualTo("PENDING");assertThat(queue.prepare()).isEmpty();
        jdbc.update("update trial_activation_emails set next_attempt_at=NOW()-INTERVAL '1 second' where trial_id=?",t.trialId());
        var second=queue.prepare().orElseThrow();assertThat(second.attempt()).isEqualTo(2);
        jdbc.update("update trial_activation_emails set next_attempt_at=NOW()-INTERVAL '1 second' where trial_id=?",t.trialId());
        var third=queue.prepare().orElseThrow();assertThat(third.attempt()).isEqualTo(3);
        queue.sent(second);assertThat(state(t.trialId())).isEqualTo("SENDING");
        queue.sent(third);assertThat(state(t.trialId())).isEqualTo("SENT");
    }
    @Test void reenvioAtendeTrialAnteriorSemFila() {
        var t=create();jdbc.update("delete from trial_activation_emails where trial_id=?",t.trialId());
        queue.resend(t.tenantId(),"ana@example.test");assertThat(state(t.trialId())).isEqualTo("PENDING");
    }
}
