package com.traxup.tplug.erp.auth.api;

import com.traxup.tplug.erp.auth.RbacApplicationService;
import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.perfil.Perfil;
import com.traxup.tplug.erp.permissao.Permissao;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rbac")
@PreAuthorize("hasAuthority('RBAC_GERENCIAR')")
public class RbacAdminController {

    private final RbacApplicationService rbacApplicationService;
    private final TenantContext tenantContext;

    public RbacAdminController(RbacApplicationService rbacApplicationService, TenantContext tenantContext) {
        this.rbacApplicationService = rbacApplicationService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/perfis")
    public List<PerfilResponse> listarPerfis() {
        return rbacApplicationService.listarPerfis(tenantContext.tenantId()).stream()
                .map(PerfilResponse::from)
                .toList();
    }

    @GetMapping("/permissoes")
    public List<PermissaoResponse> listarPermissoes() {
        return rbacApplicationService.listarPermissoes().stream()
                .map(PermissaoResponse::from)
                .toList();
    }

    @PostMapping("/perfis")
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilResponse criarPerfil(@RequestBody CriarPerfilRequest request) {
        Perfil perfil = rbacApplicationService.criarPerfil(
                tenantContext.tenantId(), request.nome(), request.descricao());
        return PerfilResponse.from(perfil);
    }

    @PostMapping("/perfis/{perfilId}/permissoes/{permissaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atribuirPermissaoAoPerfil(
            @PathVariable UUID perfilId,
            @PathVariable UUID permissaoId) {
        rbacApplicationService.atribuirPermissaoAoPerfil(
                tenantContext.tenantId(), perfilId, permissaoId);
    }

    @PostMapping("/usuarios/{usuarioId}/perfis/{perfilId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atribuirPerfilAoUsuario(
            @PathVariable UUID usuarioId,
            @PathVariable UUID perfilId) {
        rbacApplicationService.atribuirPerfilAoUsuario(
                tenantContext.tenantId(), usuarioId, perfilId);
    }

    public record CriarPerfilRequest(String nome, String descricao) {
    }

    public record PerfilResponse(UUID id, String nome, String descricao, boolean ativo) {
        static PerfilResponse from(Perfil perfil) {
            return new PerfilResponse(perfil.getId(), perfil.getNome(), perfil.getDescricao(), perfil.isAtivo());
        }
    }

    public record PermissaoResponse(UUID id, String chave, String descricao) {
        static PermissaoResponse from(Permissao permissao) {
            return new PermissaoResponse(permissao.getId(), permissao.getChave(), permissao.getDescricao());
        }
    }
}
