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

    public ResumoPainel resumir(UUID tenantId, UUID contaId) {
        List<ItemPainel> itens = listar(tenantId, contaId);
        long saudaveis = itens.stream().filter(item -> "SAUDAVEL".equals(item.saude().status())).count();
        long atencao = itens.stream().filter(item -> "ATENCAO".equals(item.saude().status())).count();
        long semExecucao = itens.stream().filter(item -> "SEM_EXECUCAO".equals(item.saude().status())).count();
        return new ResumoPainel(itens.size(), saudaveis, atencao, semExecucao);
    }

    public record ItemPainel(IntegracaoFinanceira integracao,
                             IntegracaoFinanceiraObservabilidadeService.ResumoSaude saude) {}

    public record ResumoPainel(long total, long saudaveis, long atencao, long semExecucao) {}
}
