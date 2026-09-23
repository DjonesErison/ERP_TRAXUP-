package com.traxup.tplug.erp.trial;

import com.traxup.tplug.erp.auth.*;
import com.traxup.tplug.erp.trial.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"trial.mail.enabled=true","trial.mail.initial-delay-ms=3600000"})
@AutoConfigureMockMvc
@Transactional
class TrialAccessRecoveryIntegrationTest {
    @Autowired TrialProvisioningService provisioning;
    @Autowired AccessRecoveryQueue queue;
    @Autowired AccessRecoveryMail mail;
    @Autowired AuthApplicationService auth;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired jakarta.persistence.EntityManager em;
    TrialCadastroRequest request(String document) { return new TrialCadastroRequest("Ana","Loja","Loja LTDA",document,"87999999999","ana@example.test",null,1,true,"2026-09",UUID.randomUUID().toString()); }
    TrialCadastroResponse create() { return provisioning.provisionar(request("123.456.789-01")); }
    void active(UUID id) { em.flush(); jdbc.update("UPDATE trials_saas SET admin_ativado_em=NOW() WHERE id=?",id); em.clear(); }
    @Test void documentoNormalizadoBloqueiaAntesDeCriarOutroTenant() {
        create(); long before=jdbc.queryForObject("SELECT count(*) FROM tenants",Long.class);
        assertThatThrownBy(()->provisioning.provisionar(request("12345678901"))).hasMessage("Empresa já cadastrada");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM tenants",Long.class)).isEqualTo(before);
    }
    @Test void recuperacaoNaoEnumeraNemRevelaEmailCodigoTokenOuStatus() throws Exception {
        create();
        for(String body:new String[]{"{\"documento\":\"12345678901\",\"email\":\"ana@example.test\"}","{\"documento\":\"12345678901\",\"email\":\"wrong@example.test\"}","{\"documento\":\"99999999999\",\"email\":\"ana@example.test\"}"})
            mvc.perform(post("/api/public/trials/recuperar-acesso").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isAccepted()).andExpect(content().string(""));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM access_recovery_emails",Integer.class)).isEqualTo(1);
    }
    @Test void pendenteReenviaAtivacaoComTrintaMinutos() {
        create(); queue.requestDocument("123.456.789-01","ANA@example.test"); var d=queue.prepare().orElseThrow();
        assertThat(d.action()).isEqualTo("ACTIVATE"); assertThat(d.code()).matches("[0-9]{4}");
        assertThat(mail.message(d).getTo()).containsExactly("ana@example.test");
        assertThat(mail.message(d).getText()).contains("/ativar#token=", "30 minutos");
        em.flush();
        assertThat(jdbc.queryForObject("SELECT max(extract(epoch from (expira_em-criado_em))) FROM ativacao_admin_tokens",Double.class)).isBetween(1790.0,1801.0);
        auth.confirmarAtivacaoAdministrador(d.token(),"SenhaNova123!");
        assertThatThrownBy(()->auth.confirmarAtivacaoAdministrador(d.token(),"OutraSenha123!")).isInstanceOf(RuntimeException.class);
    }
    @Test void ativoRecebeCodigoERedefineSenhaUmaUnicaVez() {
        var t=create(); active(t.trialId()); queue.requestDocument("12345678901","ana@example.test"); var d=queue.prepare().orElseThrow();
        assertThat(d.action()).isEqualTo("RECOVER"); assertThat(d.code()).isEqualTo(t.codigoEmpresa());
        assertThat(mail.message(d).getText()).contains("/recuperar#token=", "Empresa: "+t.codigoEmpresa());
        em.flush();
        assertThat(jdbc.queryForObject("SELECT max(extract(epoch from (expira_em-criado_em))) FROM recuperacao_senha_tokens",Double.class)).isBetween(1790.0,1801.0);
        assertThat(jdbc.queryForObject("SELECT token_hash FROM recuperacao_senha_tokens LIMIT 1",String.class)).hasSize(64).isNotEqualTo(d.token());
        auth.confirmarRecuperacao(d.token(),"SenhaNova123!");
        assertThat(auth.loginPorEmpresa(t.codigoEmpresa(),null,"ana@example.test","SenhaNova123!").accessToken()).isNotBlank();
        assertThatThrownBy(()->auth.confirmarRecuperacao(d.token(),"OutraSenha123!")).isInstanceOf(RuntimeException.class);
    }
    @Test void expiradoOrientaContratacaoSemGerarToken() {
        var t=create(); em.flush(); jdbc.update("UPDATE trials_saas SET inicio_em=NOW()-INTERVAL '8 days',expira_em=NOW()-INTERVAL '1 day' WHERE id=?",t.trialId());em.clear();
        queue.requestDocument("12345678901","ana@example.test");var d=queue.prepare().orElseThrow();
        assertThat(d.action()).isEqualTo("EXPIRED"); assertThat(d.token()).isNull();
        assertThat(mail.message(d).getText()).contains("reativar ou contratar").doesNotContain("#token=");
    }
    @Test void rejeitaTokenExpiradoELimitaEnvios() {
        var t=create(); active(t.trialId()); queue.requestDocument("12345678901","ana@example.test");var d=queue.prepare().orElseThrow();queue.sent(d);
        queue.requestDocument("12345678901","ana@example.test");assertThat(queue.prepare()).isEmpty();
        em.flush();jdbc.update("UPDATE recuperacao_senha_tokens SET expira_em=NOW()-INTERVAL '1 second'");em.clear();
        assertThatThrownBy(()->auth.confirmarRecuperacao(d.token(),"SenhaNova123!")).isInstanceOf(RuntimeException.class);
    }
}
