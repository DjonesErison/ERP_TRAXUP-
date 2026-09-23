package com.traxup.tplug.erp.onboarding;

import com.traxup.tplug.erp.trial.TrialProvisioningService;
import com.traxup.tplug.erp.trial.api.TrialCadastroRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="trial.mail.enabled=false")
@AutoConfigureMockMvc
@Transactional
class OnboardingIntegrationTest {
    @Autowired TrialProvisioningService provisioning;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;

    @Test void administradorAtivadoConcluiPrimeiraUnidadeSemDuplicarAoRepetir() throws Exception {
        var trial = provisioning.provisionar(new TrialCadastroRequest(
            "Ana", "Loja", "Loja LTDA", "12345678000199", "87999999999",
            "ana@example.test", "Varejo", 1, true, "2026-09", UUID.randomUUID().toString()));
        mvc.perform(post("/api/v1/auth/ativacao-admin/confirmar").contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + trial.ativacaoToken() + "\",\"novaSenha\":\"SenhaTeste123!\"}"))
            .andExpect(status().isNoContent());
        var login = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"tenantId\":\"" + trial.tenantId() + "\",\"email\":\"ana@example.test\",\"senha\":\"SenhaTeste123!\"}"))
            .andExpect(status().isOk()).andReturn();
        String bearer = "Bearer " + mapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(post("/api/v1/onboarding/concluir").header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nomeFilial\":\"Loja Matriz\",\"cnpj\":\"12.345.678/0001-99\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.concluido").value(true));
        }
        mvc.perform(get("/api/v1/onboarding").header("Authorization", bearer))
            .andExpect(status().isOk()).andExpect(jsonPath("$.concluido").value(true))
            .andExpect(jsonPath("$.filialConfigurada").value(true));
        assertThat(jdbc.queryForObject("select count(*) from filiais where tenant_id=?", Integer.class, trial.tenantId())).isEqualTo(1);
        assertThat(jdbc.queryForObject("select cnpj from filiais where tenant_id=?", String.class, trial.tenantId())).isEqualTo("12345678000199");
        assertThat(jdbc.queryForObject("select count(*) from usuario_filiais uf join trials_saas t on t.tenant_id=uf.tenant_id and t.administrador_id=uf.usuario_id where t.id=?", Integer.class, trial.trialId())).isEqualTo(1);
        assertThat(jdbc.queryForObject("select onboarding_status from trials_saas where id=?", String.class, trial.trialId())).isEqualTo("CONCLUIDO");
    }
}
