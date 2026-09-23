package com.traxup.tplug.erp.bootstrap;

import com.traxup.tplug.erp.auth.RbacApplicationService;
import com.traxup.tplug.erp.perfil.Perfil;
import com.traxup.tplug.erp.permissao.PermissaoRepository;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import com.traxup.tplug.erp.usuario.Usuario;
import com.traxup.tplug.erp.usuario.UsuarioApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class BootstrapAdminService {

    private final TenantRepository tenantRepository;
    private final UsuarioApplicationService usuarioApplicationService;
    private final RbacApplicationService rbacApplicationService;
    private final PermissaoRepository permissaoRepository;

    public BootstrapAdminService(
            TenantRepository tenantRepository,
            UsuarioApplicationService usuarioApplicationService,
            RbacApplicationService rbacApplicationService,
            PermissaoRepository permissaoRepository) {
        this.tenantRepository = tenantRepository;
        this.usuarioApplicationService = usuarioApplicationService;
        this.rbacApplicationService = rbacApplicationService;
        this.permissaoRepository = permissaoRepository;
    }

    @Transactional
    public void executar(String tenantNome, String adminNome, String adminEmail, String adminSenha) {
        validarTexto(tenantNome, "BOOTSTRAP_ADMIN_TENANT_NAME");
        validarTexto(adminNome, "BOOTSTRAP_ADMIN_NAME");
        validarTexto(adminEmail, "BOOTSTRAP_ADMIN_EMAIL");
        validarSenha(adminSenha);

        if (tenantRepository.count() > 0) {
            throw new IllegalStateException("Bootstrap recusado: ja existe tenant cadastrado");
        }

        Tenant tenant = tenantRepository.save(new Tenant(tenantNome.trim()));
        Usuario admin = usuarioApplicationService.criar(
                tenant.getId(),
                adminNome.trim(),
                adminEmail.trim().toLowerCase(Locale.ROOT),
                adminSenha);

        Perfil perfilAdmin = rbacApplicationService.criarPerfil(
                tenant.getId(),
                "ADMIN",
                "Administrador inicial criado pelo bootstrap seguro");

        permissaoRepository.findAll().forEach(permissao ->
                rbacApplicationService.atribuirPermissaoAoPerfil(
                        tenant.getId(), perfilAdmin.getId(), permissao.getId()));

        rbacApplicationService.atribuirPerfilAoUsuario(
                tenant.getId(), admin.getId(), perfilAdmin.getId());
    }

    private void validarTexto(String valor, String nomeVariavel) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(nomeVariavel + " deve ser informado quando o bootstrap estiver habilitado");
        }
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < 12) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD deve possuir pelo menos 12 caracteres");
        }
    }
}
