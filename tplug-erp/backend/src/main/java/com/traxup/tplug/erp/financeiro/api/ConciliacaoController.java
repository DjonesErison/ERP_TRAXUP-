package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.financeiro.ConciliacaoApplicationService;
import com.traxup.tplug.erp.financeiro.ofx.OfxExtratoParser;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financeiro/conciliacao")
public class ConciliacaoController {
    private final ConciliacaoApplicationService service;
    private final TenantContext tenantContext;
    private final OfxExtratoParser ofxParser;

    public ConciliacaoController(ConciliacaoApplicationService service, TenantContext tenantContext,
                                 OfxExtratoParser ofxParser) {
        this.service = service;
        this.tenantContext = tenantContext;
        this.ofxParser = ofxParser;
    }

    @GetMapping("/contas/{contaId}/lancamentos")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_LER')")
    public List<ConciliacaoLancamentoResponse> listar(
            @PathVariable UUID contaId,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String natureza,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim) {
        return service.listar(tenantContext.tenantId(), contaId, origem, natureza, status, tipo, inicio, fim).stream()
                .map(ConciliacaoLancamentoResponse::from).toList();
    }

    @GetMapping("/contas/{contaId}/resumo")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_LER')")
    public ConciliacaoResumoResponse resumo(
            @PathVariable UUID contaId,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String natureza,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim) {
        return ConciliacaoResumoResponse.from(service.resumir(
                tenantContext.tenantId(), contaId, origem, natureza, status, tipo, inicio, fim));
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
        return importarItens(contaId, itens);
    }

    @PostMapping("/contas/{contaId}/ofx")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_EDITAR')")
    public List<ConciliacaoLancamentoResponse> importarOfx(@PathVariable UUID contaId,
                                                           @Valid @RequestBody ImportarOfxRequest request) {
        return importarItens(contaId, ofxParser.parse(request.conteudo()));
    }

    @PostMapping("/lancamentos/{lancamentoId}/classificar")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_EDITAR')")
    public ConciliacaoLancamentoResponse classificar(@PathVariable UUID lancamentoId,
                                                      @Valid @RequestBody ClassificarConciliacaoLancamentoRequest request) {
        return ConciliacaoLancamentoResponse.from(service.classificar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), lancamentoId, request.natureza()));
    }

    @PostMapping("/lancamentos/{lancamentoId}/conciliar")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_EDITAR')")
    public ConciliacaoLancamentoResponse conciliar(@PathVariable UUID lancamentoId,
                                                    @Valid @RequestBody ConciliarLancamentoRequest request) {
        return ConciliacaoLancamentoResponse.from(service.conciliar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), lancamentoId, request.movimentoId()));
    }

    private List<ConciliacaoLancamentoResponse> importarItens(
            UUID contaId, List<ConciliacaoApplicationService.ImportacaoLancamento> itens) {
        return service.importarLote(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId, itens).stream()
                .map(ConciliacaoLancamentoResponse::from).toList();
    }
}
