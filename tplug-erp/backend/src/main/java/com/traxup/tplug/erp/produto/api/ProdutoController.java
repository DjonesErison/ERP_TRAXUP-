package com.traxup.tplug.erp.produto.api;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoApplicationService;
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
@RequestMapping("/api/v1/produtos")
public class ProdutoController {

    private final ProdutoApplicationService produtoApplicationService;
    private final AuditoriaApplicationService auditoriaApplicationService;
    private final TenantContext tenantContext;

    public ProdutoController(ProdutoApplicationService produtoApplicationService,
                             AuditoriaApplicationService auditoriaApplicationService,
                             TenantContext tenantContext) {
        this.produtoApplicationService = produtoApplicationService;
        this.auditoriaApplicationService = auditoriaApplicationService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public List<ProdutoResponse> listar() {
        UUID tenantId = tenantContext.tenantId();
        return produtoApplicationService.listar(tenantId).stream()
                .map(produto -> ProdutoResponse.from(tenantId, produto))
                .toList();
    }

    @GetMapping("/{produtoId}")
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public ProdutoResponse buscarPorId(@PathVariable UUID produtoId) {
        UUID tenantId = tenantContext.tenantId();
        return ProdutoResponse.from(tenantId, produtoApplicationService.buscarPorId(tenantId, produtoId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_CRIAR')")
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody CriarProdutoRequest request) {
        UUID tenantId = tenantContext.tenantId();
        Produto produto = produtoApplicationService.criar(
                tenantId, request.codigo(), request.descricao(), request.grupo(), request.ncm(),
                request.vendaPrc(), request.compraPrc(), request.codigoBarra(), request.unidade());

        auditoriaApplicationService.registrar(
                tenantId,
                tenantContext.usuarioIdOuNulo(),
                null,
                null,
                "CRIAR",
                "PRODUTO",
                produto.getId(),
                null);

        return ResponseEntity.created(URI.create("/api/v1/produtos/" + produto.getId()))
                .body(ProdutoResponse.from(tenantId, produto));
    }

    @PatchMapping("/{produtoId}/desativar")
    @PreAuthorize("hasAuthority('PRODUTO_DESATIVAR')")
    public ProdutoResponse desativar(@PathVariable UUID produtoId) {
        UUID tenantId = tenantContext.tenantId();
        Produto produto = produtoApplicationService.desativar(tenantId, produtoId);

        auditoriaApplicationService.registrar(
                tenantId,
                tenantContext.usuarioIdOuNulo(),
                null,
                null,
                "DESATIVAR",
                "PRODUTO",
                produto.getId(),
                null);

        return ProdutoResponse.from(tenantId, produto);
    }
}
