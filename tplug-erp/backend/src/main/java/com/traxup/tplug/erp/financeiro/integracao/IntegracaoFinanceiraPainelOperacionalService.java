package com.traxup.tplug.erp.financeiro.integracao;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class IntegracaoFinanceiraPainelOperacionalService {
    private final IntegracaoFinanceiraApplicationService integracaoService;
    private final IntegracaoFinanceiraObservabilidadeService observabilidadeService;

    public IntegracaoFinanceiraPainelOperacionalService(
            IntegracaoFinanceiraApplicationService integracaoService,
            IntegracaoFinanceiraObservabilidadeService observabilidadeService) {
        this.integracaoService = integracaoService;
        this.observabilidadeService = observabilidadeService;
    }

    public List<ItemPainel> listar(UUID tenantId, UUID contaId) {
        return integracaoService.listar(tenantId, contaId).stream()
                .map(integracao -> new ItemPainel(
                        integracao,
                        observabilidadeService.resumirSaude(tenantId, integracao.getId())))
                .toList();
    }

    public record ItemPainel(IntegracaoFinanceira integracao,
                             IntegracaoFinanceiraObservabilidadeService.ResumoSaude saude) {}
}
