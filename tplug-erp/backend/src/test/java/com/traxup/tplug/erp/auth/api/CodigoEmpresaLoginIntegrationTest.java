package com.traxup.tplug.erp.auth.api;

import com.traxup.tplug.erp.tenant.*;
import com.traxup.tplug.erp.usuario.UsuarioApplicationService;
import com.traxup.tplug.erp.trial.TrialProvisioningService;
import com.traxup.tplug.erp.trial.api.TrialCadastroRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="trial.mail.enabled=false")
@AutoConfigureMockMvc
@Transactional
class CodigoEmpresaLoginIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired TenantRepository tenants;
    @Autowired UsuarioApplicationService usuarios;
    @Autowired JwtDecoder decoder;
    @Autowired ObjectMapper mapper;
    @Autowired TrialProvisioningService trials;
    String credentials(String identifier,String password) {return "{"+identifier+",\"email\":\"ANA@example.test\",\"senha\":\""+password+"\"}";}
    @Test void codigoSelecionaTenantCorretoComMesmoEmailEJwtPreservaUuid() throws Exception {
        var a=tenants.saveAndFlush(new Tenant("A"));var b=tenants.saveAndFlush(new Tenant("B"));
        usuarios.criar(a.getId(),"Ana","ana@example.test","SenhaDaEmpresaA!");
        usuarios.criar(b.getId(),"Ana","ana@example.test","SenhaDaEmpresaB!");
        String id="\"codigoEmpresa\":\""+a.getCodigoEmpresa()+"\"";
        var r=mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials(id,"SenhaDaEmpresaA!"))).andExpect(status().isOk()).andReturn();
        String token=mapper.readTree(r.getResponse().getContentAsString()).get("accessToken").asText();
        assertThat(decoder.decode(token).getClaimAsString("tenant_id")).isEqualTo(a.getId().toString());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials(id,"SenhaDaEmpresaB!"))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials(id+",\"tenantId\":\""+b.getId()+"\"","SenhaDaEmpresaA!"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials("\"tenantId\":\""+a.getId()+"\"","SenhaDaEmpresaA!"))).andExpect(status().isOk());
    }
    @ParameterizedTest @ValueSource(strings={"", "123", "12345", "abcd", "１２３４", " 1234", "1234 "})
    void rejeitaCodigoForaDoFormato(String code) throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials("\"codigoEmpresa\":\""+code+"\"","SenhaTeste123!"))).andExpect(status().isBadRequest());
    }
    @Test void codigoDesconhecidoNaoAutenticaEReenvioRecuperacaoSaoNeutros() throws Exception {
        // Allocate no tenant for this code: all fixtures roll back after each test.
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(credentials("\"codigoEmpresa\":\"9999\"","SenhaTeste123!"))).andExpect(status().isUnauthorized());
        for(String path:java.util.List.of("/api/public/trials/reenviar-ativacao", "/api/v1/auth/recuperacao-senha/solicitar")) {
            mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{\"codigoEmpresa\":\"9999\",\"email\":\"unknown@example.test\"}")).andExpect(status().isAccepted());
        }
    }
    @Test void trialPublicoRetornaCodigoEstavelAtivaEFazLogin() throws Exception {
        String key=UUID.randomUUID().toString();
        String payload="""
            {"nomeCompleto":"Ana", "nomeEmpresa":"Loja", "razaoSocial":"Loja LTDA", "documento":"12345678000199",
             "telefone":"87999999999", "email":"ana@example.test", "segmento":"Varejo", "quantidadeLojas":1,
             "aceitouTermos":true, "termosVersao":"2026-09", "idempotencyKey":"%s"}
            """.formatted(key);
        var result=mvc.perform(post("/api/public/trials").contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isCreated()).andReturn();
        var json=mapper.readTree(result.getResponse().getContentAsString());String code=json.get("codigoEmpresa").asText();
        assertThat(code).matches("[0-9]{4}");
        assertThat(tenants.findByCodigoEmpresa(code).orElseThrow().getId().toString()).isEqualTo(json.get("tenantId").asText());
        mvc.perform(post("/api/public/trials").contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isCreated()).andExpect(jsonPath("$.codigoEmpresa").value(code)).andExpect(jsonPath("$.trialId").value(json.get("trialId").asText()));
        mvc.perform(post("/api/v1/auth/ativacao-admin/confirmar").contentType(MediaType.APPLICATION_JSON).content("{\"token\":\""+json.get("ativacaoToken").asText()+"\",\"novaSenha\":\"SenhaTeste123!\"}")).andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials("\"codigo_empresa\":\""+code+"\"","SenhaTeste123!"))).andExpect(status().isOk());
    }
}
