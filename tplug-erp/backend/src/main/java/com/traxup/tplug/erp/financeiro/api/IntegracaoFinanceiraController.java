package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.financeiro.integracao.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financeiro/integracoes")
public class IntegracaoFinanceiraController {
    private final IntegracaoFinanceiraApplicationService service;
    private final IntegracaoFinanceiraSincronizacaoService sincronizacaoService;
    private final IntegracaoFinanceiraAdapterSincronizacaoService adapterSincronizacaoService;
    private final IntegracaoFinanceiraObservabilidadeService observabilidadeService;
    private final IntegracaoFinanceiraPainelOperacionalService painelOperacionalService;
    private final TenantContext tenantContext;

    public IntegracaoFinanceiraController(IntegracaoFinanceiraApplicationService service,
            IntegracaoFinanceiraSincronizacaoService sincronizacaoService,
            IntegracaoFinanceiraAdapterSincronizacaoService adapterSincronizacaoService,
            IntegracaoFinanceiraObservabilidadeService observabilidadeService,
            IntegracaoFinanceiraPainelOperacionalService painelOperacionalService,
            TenantContext tenantContext) {
        this.service = service; this.sincronizacaoService = sincronizacaoService;
        this.adapterSincronizacaoService = adapterSincronizacaoService; this.observabilidadeService = observabilidadeService;
        this.painelOperacionalService = painelOperacionalService; this.tenantContext = tenantContext;
    }

    @GetMapping("/contas/{contaId}") @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_LER')")
    public List<IntegracaoFinanceiraResponse> listar(@PathVariable UUID contaId) {
        return service.listar(tenantContext.tenantId(), contaId).stream().map(IntegracaoFinanceiraResponse::from).toList();
    }

    @GetMapping("/contas/{contaId}/painel") @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_LER')")
    public List<IntegracaoFinanceiraPainelOperacionalResponse> painelOperacional(@PathVariable UUID contaId) {
        return painelOperacionalService.listar(tenantContext.tenantId(), contaId).stream()
                .map(IntegracaoFinanceiraPainelOperacionalResponse::from).toList();
    }

    @GetMapping("/contas/{contaId}/painel/resumo") @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_LER')")
    public IntegracaoFinanceiraResumoPainelResponse resumoPainel(@PathVariable UUID contaId) {
        return IntegracaoFinanceiraResumoPainelResponse.from(
                painelOperacionalService.resumir(tenantContext.tenantId(), contaId));
    }

    @GetMapping("/{integracaoId}/tentativas") @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_LER')")
    public List<IntegracaoFinanceiraTentativaResponse> listarTentativas(
            @PathVariable UUID integracaoId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Instant inicio,
            @RequestParam(required = false) Instant fim) {
        return observabilidadeService.listar(tenantContext.tenantId(), integracaoId, status, inicio, fim).stream()
                .map(IntegracaoFinanceiraTentativaResponse::from).toList();
    }

    @GetMapping("/{integracaoId}/saude") @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_LER')")
    public IntegracaoFinanceiraSaudeResponse consultarSaude(@PathVariable UUID integracaoId) {
        return IntegracaoFinanceiraSaudeResponse.from(
                observabilidadeService.resumirSaude(tenantContext.tenantId(), integracaoId));
    }

    @PostMapping("/contas/{contaId}") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_EDITAR')")
    public IntegracaoFinanceiraResponse criar(@PathVariable UUID contaId, @Valid @RequestBody CriarIntegracaoFinanceiraRequest request) {
        return IntegracaoFinanceiraResponse.from(service.criar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId,
                request.provedor(), request.identificadorExterno()));
    }

    @PostMapping("/{integracaoId}/sincronizacao") @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_EDITAR')")
    public IntegracaoFinanceiraResponse registrarSincronizacao(@PathVariable UUID integracaoId,
            @Valid @RequestBody RegistrarSincronizacaoIntegracaoRequest request) {
        return IntegracaoFinanceiraResponse.from(service.registrarSincronizacao(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(),
                integracaoId, request.checkpoint(), request.sincronizadoEm()));
    }

    @PostMapping("/{integracaoId}/sincronizar-lote") @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_EDITAR')")
    public SincronizarIntegracaoFinanceiraResponse sincronizarLote(@PathVariable UUID integracaoId,
            @Valid @RequestBody SincronizarIntegracaoFinanceiraRequest request) {
        var itens = request.lancamentos().stream().map(item -> new IntegracaoFinanceiraSincronizacaoService.LancamentoExterno(
                item.referenciaExterna(), item.tipo(), item.valor(), item.descricao(), item.ocorridoEm())).toList();
        return SincronizarIntegracaoFinanceiraResponse.from(sincronizacaoService.sincronizar(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), integracaoId, itens, request.checkpoint(), request.sincronizadoEm()));
    }

    @PostMapping("/{integracaoId}/sincronizar") @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_EDITAR')")
    public SincronizarIntegracaoFinanceiraResponse sincronizarPorAdapter(@PathVariable UUID integracaoId) {
        return SincronizarIntegracaoFinanceiraResponse.from(adapterSincronizacaoService.sincronizar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), integracaoId));
    }

    @PostMapping("/{integracaoId}/desativar") @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_EDITAR')")
    public IntegracaoFinanceiraResponse desativar(@PathVariable UUID integracaoId) {
        return IntegracaoFinanceiraResponse.from(service.desativar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), integracaoId));
    }
}
