package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class IntegracaoFinanceiraAdapterSincronizacaoService {
    private final IntegracaoFinanceiraRepository repository;
    private final IntegracaoFinanceiraAdapterRegistry registry;
    private final IntegracaoFinanceiraSincronizacaoService sincronizacaoService;
    private final IntegracaoFinanceiraObservabilidadeService observabilidadeService;

    @Autowired
    public IntegracaoFinanceiraAdapterSincronizacaoService(IntegracaoFinanceiraRepository repository,
                                                            IntegracaoFinanceiraAdapterRegistry registry,
                                                            IntegracaoFinanceiraSincronizacaoService sincronizacaoService,
                                                            IntegracaoFinanceiraObservabilidadeService observabilidadeService) {
        this.repository = repository; this.registry = registry; this.sincronizacaoService = sincronizacaoService;
        this.observabilidadeService = observabilidadeService;
    }

    IntegracaoFinanceiraAdapterSincronizacaoService(IntegracaoFinanceiraRepository repository,
                                                     IntegracaoFinanceiraAdapterRegistry registry,
                                                     IntegracaoFinanceiraSincronizacaoService sincronizacaoService) {
        this(repository, registry, sincronizacaoService, null);
    }

    public IntegracaoFinanceiraSincronizacaoService.ResultadoSincronizacao sincronizar(
            UUID tenantId, UUID usuarioId, UUID integracaoId) {
        IntegracaoFinanceira integracao = repository.findByIdAndTenantId(integracaoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Integracao financeira nao encontrada para o tenant informado"));
        if (!integracao.isAtivo()) {
            throw new RecursoConflitanteException("Integracao financeira inativa nao pode ser sincronizada");
        }

        Instant inicio = Instant.now();
        int quantidade = 0;
        try {
            IntegracaoFinanceiraAdapter adapter = registry.obter(integracao.getProvedor());
            IntegracaoFinanceiraAdapter.Resultado resultado = adapter.buscar(integracao);
            quantidade = resultado.lancamentos().size();
            var sincronizado = sincronizacaoService.sincronizar(
                    tenantId, usuarioId, integracaoId, resultado.lancamentos(),
                    resultado.checkpoint(), resultado.sincronizadoEm());
            Instant fim = Instant.now();
            if (observabilidadeService != null) observabilidadeService.sucesso(
                    integracao, quantidade, duracao(inicio, fim), inicio, fim);
            return sincronizado;
        } catch (RuntimeException erro) {
            Instant fim = Instant.now();
            if (observabilidadeService != null) observabilidadeService.falha(
                    integracao, quantidade, duracao(inicio, fim), erro, inicio, fim);
            throw erro;
        }
    }

    private long duracao(Instant inicio, Instant fim) {
        return Math.max(0, Duration.between(inicio, fim).toMillis());
    }
}
