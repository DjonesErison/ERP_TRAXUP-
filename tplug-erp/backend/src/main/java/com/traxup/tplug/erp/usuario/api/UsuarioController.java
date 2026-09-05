package com.traxup.tplug.erp.usuario.api;

import com.traxup.tplug.erp.usuario.Usuario;
import com.traxup.tplug.erp.usuario.UsuarioApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    private final UsuarioApplicationService usuarioApplicationService;

    public UsuarioController(UsuarioApplicationService usuarioApplicationService) {
        this.usuarioApplicationService = usuarioApplicationService;
    }

    @GetMapping
    public List<UsuarioResponse> listar(@RequestHeader(TENANT_HEADER) UUID tenantId) {
        return usuarioApplicationService.listar(tenantId).stream()
                .map(usuario -> UsuarioResponse.from(tenantId, usuario))
                .toList();
    }

    @GetMapping("/{usuarioId}")
    public UsuarioResponse buscarPorId(
            @RequestHeader(TENANT_HEADER) UUID tenantId,
            @PathVariable UUID usuarioId) {
        return UsuarioResponse.from(
                tenantId,
                usuarioApplicationService.buscarPorId(tenantId, usuarioId));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(
            @RequestHeader(TENANT_HEADER) UUID tenantId,
            @Valid @RequestBody CriarUsuarioRequest request) {
        Usuario usuario = usuarioApplicationService.criar(
                tenantId,
                request.nome(),
                request.email(),
                request.senha());

        return ResponseEntity
                .created(URI.create("/api/v1/usuarios/" + usuario.getId()))
                .body(UsuarioResponse.from(tenantId, usuario));
    }

    @PatchMapping("/{usuarioId}/desativar")
    public UsuarioResponse desativar(
            @RequestHeader(TENANT_HEADER) UUID tenantId,
            @PathVariable UUID usuarioId) {
        return UsuarioResponse.from(
                tenantId,
                usuarioApplicationService.desativar(tenantId, usuarioId));
    }
}
