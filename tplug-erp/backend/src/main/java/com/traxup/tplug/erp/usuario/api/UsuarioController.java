package com.traxup.tplug.erp.usuario.api;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.usuario.Usuario;
import com.traxup.tplug.erp.usuario.UsuarioApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioApplicationService usuarioApplicationService;
    private final AuditoriaApplicationService auditoriaApplicationService;
    private final TenantContext tenantContext;

    public UsuarioController(
            UsuarioApplicationService usuarioApplicationService,
            AuditoriaApplicationService auditoriaApplicationService,
            TenantContext tenantContext) {
        this.usuarioApplicationService = usuarioApplicationService;
        this.auditoriaApplicationService = auditoriaApplicationService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_LER')")
    public List<UsuarioResponse> listar() {
        UUID tenantId = tenantContext.tenantId();
        return usuarioApplicationService.listar(tenantId).stream()
                .map(usuario -> UsuarioResponse.from(tenantId, usuario))
                .toList();
    }

    @GetMapping("/{usuarioId}")
    @PreAuthorize("hasAuthority('USUARIO_LER')")
    public UsuarioResponse buscarPorId(@PathVariable UUID usuarioId) {
        UUID tenantId = tenantContext.tenantId();
        return UsuarioResponse.from(
                tenantId,
                usuarioApplicationService.buscarPorId(tenantId, usuarioId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USUARIO_CRIAR')")
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CriarUsuarioRequest request) {
        UUID tenantId = tenantContext.tenantId();
        Usuario usuario = usuarioApplicationService.criar(
                tenantId,
                request.nome(),
                request.email(),
                request.senha());

        auditoriaApplicationService.registrar(
                tenantId,
                tenantContext.usuarioIdOuNulo(),
                null,
                null,
                "CRIAR",
                "USUARIO",
                usuario.getId(),
                null);

        return ResponseEntity
                .created(URI.create("/api/v1/usuarios/" + usuario.getId()))
                .body(UsuarioResponse.from(tenantId, usuario));
    }

    @PatchMapping("/{usuarioId}/desativar")
    @PreAuthorize("hasAuthority('USUARIO_DESATIVAR')")
    public UsuarioResponse desativar(@PathVariable UUID usuarioId) {
        UUID tenantId = tenantContext.tenantId();
        Usuario usuario = usuarioApplicationService.desativar(tenantId, usuarioId);

        auditoriaApplicationService.registrar(
                tenantId,
                tenantContext.usuarioIdOuNulo(),
                null,
                null,
                "DESATIVAR",
                "USUARIO",
                usuario.getId(),
                null);

        return UsuarioResponse.from(tenantId, usuario);
    }
}
