package com.traxup.tplug.erp.estoque.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.estoque.EstoqueMovimentacaoApplicationService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/estoque/movimentacoes")
public class EstoqueMovimentacaoController {

    private final EstoqueMovimentacaoApplicationService service;
    private final TenantContext tenantContext;

    public EstoqueMovimentacaoController(EstoqueMovimentacaoApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ESTOQUE_MOVIMENTAR')")
    public EstoqueMovimentacaoResponse movimentar(@Valid @RequestBody MovimentarEstoqueRequest request) {
        return EstoqueMovimentacaoResponse.from(service.movimentar(
                tenantContext.tenantId(), request.filialId(), request.tipoItem(), request.itemId(),
                request.tipoMovimento(), request.quantidade(), request.motivo(), tenantContext.usuarioIdOuNulo()));
    }

    @GetMapping("/filiais/{filialId}")
    @PreAuthorize("hasAuthority('ESTOQUE_MOVIMENTO_LER')")
    public List<EstoqueMovimentacaoResponse> listar(@PathVariable UUID filialId) {
        return service.listar(tenantContext.tenantId(), filialId).stream()
                .map(EstoqueMovimentacaoResponse::from)
                .toList();
    }
}
