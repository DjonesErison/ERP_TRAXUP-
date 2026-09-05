package com.traxup.tplug.erp.auth.api;

import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import com.traxup.tplug.erp.usuario.Usuario;
import com.traxup.tplug.erp.usuario.UsuarioApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UsuarioApplicationService usuarioApplicationService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void deveAutenticarEmitirJwtEUsarTenantDoToken() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Auth"));
        Usuario usuario = usuarioApplicationService.criar(
                tenant.getId(), "Usuario Auth", "auth@exemplo.com", "SenhaSegura123");

        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId": "%s",
                                  "email": "AUTH@EXEMPLO.COM",
                                  "senha": "SenhaSegura123"
                                }
                                """.formatted(tenant.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(resultado.getResponse().getContentAsString());
        String accessToken = json.get("accessToken").asText();

        Jwt jwt = jwtDecoder.decode(accessToken);
        assertThat(jwt.getSubject()).isEqualTo(usuario.getId().toString());
        assertThat(jwt.getClaimAsString("tenant_id")).isEqualTo(tenant.getId().toString());

        mockMvc.perform(get("/api/v1/usuarios/{usuarioId}", usuario.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(tenant.getId().toString()))
                .andExpect(jsonPath("$.email").value("auth@exemplo.com"));
    }

    @Test
    void deveRotacionarRefreshTokenEInvalidarOAnterior() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Refresh"));
        usuarioApplicationService.criar(
                tenant.getId(), "Usuario Refresh", "refresh@exemplo.com", "SenhaSegura123");

        JsonNode login = login(tenant, "refresh@exemplo.com", "SenhaSegura123");
        String refreshTokenAntigo = login.get("refreshToken").asText();

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "%s"}
                                """.formatted(refreshTokenAntigo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode refresh = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        String refreshTokenNovo = refresh.get("refreshToken").asText();
        assertThat(refreshTokenNovo).isNotEqualTo(refreshTokenAntigo);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "%s"}
                                """.formatted(refreshTokenAntigo)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Falha de autenticacao"));
    }

    @Test
    void deveRevogarRefreshTokenNoLogout() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Logout"));
        usuarioApplicationService.criar(
                tenant.getId(), "Usuario Logout", "logout@exemplo.com", "SenhaSegura123");

        JsonNode login = login(tenant, "logout@exemplo.com", "SenhaSegura123");
        String accessToken = login.get("accessToken").asText();
        String refreshToken = login.get("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void naoDeveAutenticarComSenhaIncorreta() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Senha Invalida"));
        usuarioApplicationService.criar(
                tenant.getId(), "Usuario Senha", "senha@exemplo.com", "SenhaSegura123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId": "%s",
                                  "email": "senha@exemplo.com",
                                  "senha": "SenhaErrada123"
                                }
                                """.formatted(tenant.getId())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Falha de autenticacao"));
    }

    private JsonNode login(Tenant tenant, String email, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId": "%s",
                                  "email": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(tenant.getId(), email, senha)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(resultado.getResponse().getContentAsString());
    }
}
