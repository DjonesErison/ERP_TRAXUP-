package com.traxup.tplug.erp.auth;

import com.traxup.tplug.erp.perfil.Perfil;
import com.traxup.tplug.erp.permissao.Permissao;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import com.traxup.tplug.erp.usuario.Usuario;
import com.traxup.tplug.erp.usuario.UsuarioApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class RbacIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UsuarioApplicationService usuarioApplicationService;

    @Autowired
    private RbacApplicationService rbacApplicationService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void deveIncluirPermissaoEfetivaNoJwt() {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant RBAC"));
        Usuario usuario = usuarioApplicationService.criar(
                tenant.getId(), "Usuario RBAC", "rbac@exemplo.com", "SenhaSegura123");
        Perfil perfil = rbacApplicationService.criarPerfil(
                tenant.getId(), "Financeiro", "Acesso ao financeiro");
        Permissao permissao = rbacApplicationService.criarPermissao(
                "financeiro.contas_receber.ler", "Consultar contas a receber");

        rbacApplicationService.atribuirPermissaoAoPerfil(tenant.getId(), perfil.getId(), permissao.getId());
        rbacApplicationService.atribuirPerfilAoUsuario(tenant.getId(), usuario.getId(), perfil.getId());

        String token = jwtService.gerarAccessToken(usuario);
        Jwt jwt = jwtDecoder.decode(token);
        List<String> permissoes = jwt.getClaimAsStringList("permissions");

        assertThat(permissoes).containsExactly("FINANCEIRO.CONTAS_RECEBER.LER");
    }

    @Test
    void naoDeveAtribuirPerfilDeOutroTenantAoUsuario() {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant A RBAC"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant B RBAC"));
        Usuario usuarioA = usuarioApplicationService.criar(
                tenantA.getId(), "Usuario A", "a.rbac@exemplo.com", "SenhaSegura123");
        Perfil perfilB = rbacApplicationService.criarPerfil(
                tenantB.getId(), "Perfil B", "Perfil de outro tenant");

        assertThatThrownBy(() -> rbacApplicationService.atribuirPerfilAoUsuario(
                tenantA.getId(), usuarioA.getId(), perfilB.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
