package com.traxup.tplug.erp.bootstrap;

import com.traxup.tplug.erp.auth.RbacApplicationService;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import com.traxup.tplug.erp.usuario.Usuario;
import com.traxup.tplug.erp.usuario.UsuarioApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BootstrapAdminIntegrationTest {

    @Autowired
    private BootstrapAdminService bootstrapAdminService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UsuarioApplicationService usuarioApplicationService;

    @Autowired
    private RbacApplicationService rbacApplicationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparTenantsCriadosPorOutrosTestes() {
        tenantRepository.deleteAll();
        tenantRepository.flush();
    }

    @Test
    void deveCriarPrimeiroTenantAdminEConcederPermissoes() {
        bootstrapAdminService.executar(
                "Tenant Inicial",
                "Administrador",
                "ADMIN@EXEMPLO.COM",
                "SenhaInicial123!");

        Tenant tenant = tenantRepository.findAll().getFirst();
        Usuario admin = usuarioApplicationService.buscarPorEmail(tenant.getId(), "admin@exemplo.com");
        List<String> permissoes = rbacApplicationService.listarPermissoesDoUsuario(tenant.getId(), admin.getId());

        assertThat(tenant.getNome()).isEqualTo("Tenant Inicial");
        assertThat(admin.getEmail()).isEqualTo("admin@exemplo.com");
        assertThat(admin.getSenhaHash()).isNotEqualTo("SenhaInicial123!");
        assertThat(passwordEncoder.matches("SenhaInicial123!", admin.getSenhaHash())).isTrue();
        assertThat(rbacApplicationService.listarPerfis(tenant.getId()))
                .anyMatch(perfil -> perfil.getNome().equals("ADMIN"));
        assertThat(permissoes).isNotEmpty();
    }

    @Test
    void deveRecusarBootstrapQuandoJaExisteTenant() {
        tenantRepository.save(new Tenant("Tenant Existente"));

        assertThatThrownBy(() -> bootstrapAdminService.executar(
                "Outro Tenant",
                "Administrador",
                "admin2@exemplo.com",
                "SenhaInicial123!"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ja existe tenant");
    }

    @Test
    void deveRecusarSenhaCurta() {
        assertThatThrownBy(() -> bootstrapAdminService.executar(
                "Tenant Inicial",
                "Administrador",
                "admin@exemplo.com",
                "curta"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pelo menos 12 caracteres");
    }
}
