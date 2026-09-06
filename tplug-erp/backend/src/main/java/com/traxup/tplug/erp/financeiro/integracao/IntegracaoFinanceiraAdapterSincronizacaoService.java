package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IntegracaoFinanceiraAdapterSincronizacaoService {
    private final IntegracaoFinanceiraRepository repository;
    private final IntegracaoFinanceiraAdapterRegistry registry;
    private final IntegracaoFinanceiraSincronizacaoService sincronizacaoService;

    public IntegracaoFinanceiraAdapterSincronizacaoService(IntegracaoFinanceiraRepository repository,
                                                            IntegracaoFinanceiraAdapterRegistry registry,
                                                            IntegracaoFinanceiraSincronizacaoService sincronizacaoService) {
        this.repository = repository;
        this.registry = registry;
        this.sincronizacaoService = sincronizacaoService;
    }

    public IntegracaoFinanceiraSincronizacaoService.ResultadoSincronizacao sincronizar(
            UUID tenantId, UUID usuarioId, UUID integracaoId) {
        IntegracaoFinanceira integracao = repository.findByIdAndTenantId(integracaoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Integracao financeira nao encontrada para o tenant informado"));
        if (!integracao.isAtivo()) {
            throw new RecursoConflitanteException("Integracao financeira inativa nao pode ser sincronizada");
        }

        IntegracaoFinanceiraAdapter adapter = registry.obter(integracao.getProvedor());
        IntegracaoFinanceiraAdapter.Resultado resultado = adapter.buscar(integracao);

        return sincronizacaoService.sincronizar(
                tenantId, usuarioId, integracaoId, resultado.lancamentos(),
                resultado.checkpoint(), resultado.sincronizadoEm());
    }
}
