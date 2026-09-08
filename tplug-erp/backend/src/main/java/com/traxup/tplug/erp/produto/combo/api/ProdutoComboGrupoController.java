package com.traxup.tplug.erp.produto.combo.api;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.produto.combo.ProdutoComboGrupoApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/produtos/{produtoId}/combo/grupos")
public class ProdutoComboGrupoController {
    private final ProdutoComboGrupoApplicationService service;
    private final AuditoriaApplicationService auditoria;
    private final TenantContext tenantContext;

    public ProdutoComboGrupoController(ProdutoComboGrupoApplicationService service,
                                       AuditoriaApplicationService auditoria,
                                       TenantContext tenantContext) {
        this.service = service;
        this.auditoria = auditoria;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public List<ProdutoComboGrupoResponse> listar(@PathVariable UUID produtoId) {
        return service.listarGrupos(tenantContext.tenantId(), produtoId).stream()
                .map(ProdutoComboGrupoResponse::from)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_CRIAR')")
    public ProdutoComboGrupoResponse criar(@PathVariable UUID produtoId,
                                            @Valid @RequestBody CriarProdutoComboGrupoRequest request) {
        UUID tenantId = tenantContext.tenantId();
        var grupo = service.criarGrupo(tenantId, produtoId, request.nome(), request.minimoEscolhas(), request.maximoEscolhas());
        auditoria.registrar(tenantId, tenantContext.usuarioIdOuNulo(), null, null,
                "CRIAR_GRUPO_ESCOLHA", "PRODUTO_COMBO", produtoId, "grupoId=" + grupo.getId());
        return ProdutoComboGrupoResponse.from(grupo);
    }

    @GetMapping("/{grupoId}/opcoes")
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public List<ProdutoComboGrupoResponse.Opcao> listarOpcoes(@PathVariable UUID produtoId,
                                                              @PathVariable UUID grupoId) {
        return service.listarOpcoes(tenantContext.tenantId(), produtoId, grupoId).stream()
                .map(ProdutoComboGrupoResponse.Opcao::from)
                .toList();
    }

    @PostMapping("/{grupoId}/opcoes")
    @PreAuthorize("hasAuthority('PRODUTO_CRIAR')")
    public ProdutoComboGrupoResponse.Opcao adicionarOpcao(@PathVariable UUID produtoId,
                                                           @PathVariable UUID grupoId,
                                                           @Valid @RequestBody AdicionarProdutoComboGrupoOpcaoRequest request) {
        UUID tenantId = tenantContext.tenantId();
        var opcao = service.adicionarOpcao(tenantId, produtoId, grupoId, request.produtoId(), request.quantidade(), request.valorAdicional());
        auditoria.registrar(tenantId, tenantContext.usuarioIdOuNulo(), null, null,
                "ADICIONAR_OPCAO_GRUPO", "PRODUTO_COMBO", produtoId,
                "grupoId=" + grupoId + ";opcaoProdutoId=" + request.produtoId());
        return ProdutoComboGrupoResponse.Opcao.from(opcao);
    }
}
