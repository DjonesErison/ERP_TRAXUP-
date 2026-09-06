package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.financeiro.ConciliacaoApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financeiro/conciliacao")
public class ConciliacaoController {
    private final ConciliacaoApplicationService service;
    private final TenantContext tenantContext;

    public ConciliacaoController(ConciliacaoApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/contas/{contaId}/lancamentos")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_LER')")
    public List<ConciliacaoLancamentoResponse> listar(@PathVariable UUID contaId) {
        return service.listar(tenantContext.tenantId(), contaId).stream()
                .map(ConciliacaoLancamentoResponse::from).toList();
    }

    @GetMapping("/lancamentos/{lancamentoId}/sugestoes")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_LER')")
    public List<ContaFinanceiraMovimentoResponse> sugestoes(@PathVariable UUID lancamentoId) {
        return service.sugerirMovimentos(tenantContext.tenantId(), lancamentoId).stream()
                .map(ContaFinanceiraMovimentoResponse::from).toList();
    }

    @PostMapping("/contas/{contaId}/lancamentos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_EDITAR')")
    public ConciliacaoLancamentoResponse importar(@PathVariable UUID contaId,
                                                   @Valid @RequestBody ImportarConciliacaoLancamentoRequest request) {
        return ConciliacaoLancamentoResponse.from(service.importar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId,
                request.origem(), request.referenciaExterna(), request.tipo(), request.valor(),
                request.descricao(), request.ocorridoEm()));
    }

    @PostMapping("/contas/{contaId}/lancamentos/lote")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_EDITAR')")
    public List<ConciliacaoLancamentoResponse> importarLote(@PathVariable UUID contaId,
                                                            @Valid @RequestBody ImportarConciliacaoLoteRequest request) {
        var itens = request.lancamentos().stream()
                .map(item -> new ConciliacaoApplicationService.ImportacaoLancamento(
                        item.origem(), item.referenciaExterna(), item.tipo(), item.valor(),
                        item.descricao(), item.ocorridoEm()))
                .toList();
        return service.importarLote(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId, itens).stream()
                .map(ConciliacaoLancamentoResponse::from).toList();
    }

    @PostMapping("/lancamentos/{lancamentoId}/conciliar")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_EDITAR')")
    public ConciliacaoLancamentoResponse conciliar(@PathVariable UUID lancamentoId,
                                                    @Valid @RequestBody ConciliarLancamentoRequest request) {
        return ConciliacaoLancamentoResponse.from(service.conciliar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), lancamentoId, request.movimentoId()));
    }
}
