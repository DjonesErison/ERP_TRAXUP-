package com.traxup.tplug.erp.auth.api;

import com.traxup.tplug.erp.auth.RbacApplicationService;
import com.traxup.tplug.erp.perfil.Perfil;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import com.traxup.tplug.erp.usuario.Usuario;
import com.traxup.tplug.erp.usuario.UsuarioApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RbacAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RbacApplicationService rbacApplicationService;

    @Autowired
    private UsuarioApplicationService usuarioApplicationService;

    @Test
    void deveListarSomentePerfisDoTenantComPermissao() throws Exception {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant RBAC API A"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant RBAC API B"));
        Perfil perfilA = rbacApplicationService.criarPerfil(tenantA.getId(), "Financeiro", "Perfil A");
        rbacApplicationService.criarPerfil(tenantB.getId(), "Compras", "Perfil B");

        mockMvc.perform(get("/api/v1/rbac/perfis")
                        .with(jwt()
                                .jwt(token -> token.claim("tenant_id", tenantA.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("RBAC_GERENCIAR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(perfilA.getId().toString()))
                .andExpect(jsonPath("$[0].nome").value("Financeiro"))
                .andExpect(jsonPath("$[?(@.nome == 'Compras')]").doesNotExist());
    }

    @Test
    void deveCriarPerfilNoTenantDoJwt() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant RBAC Criacao"));

        mockMvc.perform(post("/api/v1/rbac/perfis")
                        .with(jwt()
                                .jwt(token -> token.claim("tenant_id", tenant.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("RBAC_GERENCIAR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Gerencia\",\"descricao\":\"Perfil gerencial\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Gerencia"));
    }

    @Test
    void deveRetornarForbiddenSemRbacGerenciar() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant RBAC Sem Permissao"));

        mockMvc.perform(get("/api/v1/rbac/perfis")
                        .with(jwt().jwt(token -> token.claim("tenant_id", tenant.getId().toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void naoDeveAtribuirPerfilDeOutroTenant() throws Exception {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant RBAC Atribuicao A"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant RBAC Atribuicao B"));
        Usuario usuarioA = usuarioApplicationService.criar(
                tenantA.getId(), "Usuario A", "usuario.a@rbac.local", "SenhaSegura123");
        Perfil perfilB = rbacApplicationService.criarPerfil(tenantB.getId(), "Perfil B", "Outro tenant");

        mockMvc.perform(post("/api/v1/rbac/usuarios/{usuarioId}/perfis/{perfilId}", usuarioA.getId(), perfilB.getId())
                        .with(jwt()
                                .jwt(token -> token.claim("tenant_id", tenantA.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("RBAC_GERENCIAR"))))
                .andExpect(status().isNotFound());
    }
}
