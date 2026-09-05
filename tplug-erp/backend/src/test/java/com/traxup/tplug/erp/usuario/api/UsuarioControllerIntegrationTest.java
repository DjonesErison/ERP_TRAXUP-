package com.traxup.tplug.erp.usuario.api;

import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import com.traxup.tplug.erp.usuario.Usuario;
import com.traxup.tplug.erp.usuario.UsuarioApplicationService;
import com.traxup.tplug.erp.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioApplicationService usuarioApplicationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void deveCriarUsuarioComSenhaHashEEmailNormalizadoNoTenantDoJwt() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Usuario API"));
        String senha = "SenhaSegura123";

        mockMvc.perform(post("/api/v1/usuarios")
                        .with(jwt()
                                .jwt(token -> token.claim("tenant_id", tenant.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("USUARIO_CRIAR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario Teste",
                                  "email": "USUARIO@EXEMPLO.COM",
                                  "senha": "%s"
                                }
                                """.formatted(senha)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(tenant.getId().toString()))
                .andExpect(jsonPath("$.email").value("usuario@exemplo.com"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        Usuario usuario = usuarioRepository.findByTenantIdAndEmailIgnoreCase(
                        tenant.getId(), "usuario@exemplo.com")
                .orElseThrow();

        assertThat(usuario.getSenhaHash()).isNotEqualTo(senha);
        assertThat(passwordEncoder.matches(senha, usuario.getSenhaHash())).isTrue();
    }

    @Test
    void naoDeveBuscarUsuarioDeOutroTenant() throws Exception {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant A Usuario"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant B Usuario"));
        Usuario usuarioA = usuarioApplicationService.criar(
                tenantA.getId(), "Usuario A", "usuario.a@exemplo.com", "SenhaSegura123");

        mockMvc.perform(get("/api/v1/usuarios/{usuarioId}", usuarioA.getId())
                        .with(jwt()
                                .jwt(token -> token.claim("tenant_id", tenantB.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("USUARIO_LER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso nao encontrado"));
    }

    @Test
    void naoDevePermitirEmailDuplicadoNoMesmoTenant() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Email Unico"));
        usuarioApplicationService.criar(
                tenant.getId(), "Usuario Um", "duplicado@exemplo.com", "SenhaSegura123");

        mockMvc.perform(post("/api/v1/usuarios")
                        .with(jwt()
                                .jwt(token -> token.claim("tenant_id", tenant.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("USUARIO_CRIAR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario Dois",
                                  "email": "DUPLICADO@EXEMPLO.COM",
                                  "senha": "OutraSenha123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de recurso"));
    }

    @Test
    void deveNegarConsultaSemPermissaoDeUsuario() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Sem Permissao Usuario"));

        mockMvc.perform(get("/api/v1/usuarios")
                        .with(jwt().jwt(token -> token.claim("tenant_id", tenant.getId().toString()))))
                .andExpect(status().isForbidden());
    }
}
