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
        // A different trial must never supply the authenticated company's prefilled data.
        provisioning.provisionar(new TrialCadastroRequest(
            "Outra pessoa", "Outra loja", "Outra LTDA", "98765432000198", "87988888888",
            "outra@example.test", "Serviços", 2, true, "2026-09", UUID.randomUUID().toString()));
        mvc.perform(get("/api/v1/onboarding/configuracao").header("Authorization", bearer))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nomeUsuario").value("Ana"))
            .andExpect(jsonPath("$.empresa.razaoSocial").value("Loja LTDA"))
            .andExpect(jsonPath("$.empresa.nomeFantasia").value("Loja"))
            .andExpect(jsonPath("$.empresa.email").value("ana@example.test"))
            .andExpect(jsonPath("$.empresa.telefone").value("87999999999"))
            .andExpect(jsonPath("$.empresa.segmento").value("Varejo"))
            .andExpect(jsonPath("$.empresa.quantidadeLojas").value(1))
            .andExpect(jsonPath("$.empresa.empresaRevisada").value(false));
        mvc.perform(put("/api/v1/onboarding/configuracao/empresa").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON).content("""
            {"razaoSocial":"Loja LTDA","nomeFantasia":"Loja","cep":"56000-000",
             "endereco":"Rua do Comércio","numero":"42","complemento":"Sala 1",
             "bairro":"Centro","cidade":"Salgueiro","uf":"PE","nomeFilial":"Loja Matriz"}
            """))
            .andExpect(status().isOk()).andExpect(jsonPath("$.empresa.empresaRevisada").value(true))
            .andExpect(jsonPath("$.empresa.endereco").value("Rua do Comércio"))
            .andExpect(jsonPath("$.filial.nome").value("Loja Matriz"));
        mvc.perform(post("/api/v1/onboarding/configuracao/adiar").header("Authorization", bearer))
            .andExpect(status().isOk());
        mvc.perform(get("/api/v1/onboarding").header("Authorization", bearer))
            .andExpect(status().isOk()).andExpect(jsonPath("$.concluido").value(false));
        mvc.perform(get("/api/v1/onboarding/configuracao").header("Authorization", bearer))
            .andExpect(status().isOk()).andExpect(jsonPath("$.empresa.cidade").value("Salgueiro"))
            .andExpect(jsonPath("$.fiscalConfigurado").value(false))
            .andExpect(jsonPath("$.vendasConfiguradas").value(false));
        assertThat(jdbc.queryForObject("select count(*) from onboarding_configuracoes", Integer.class)).isEqualTo(1);
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
