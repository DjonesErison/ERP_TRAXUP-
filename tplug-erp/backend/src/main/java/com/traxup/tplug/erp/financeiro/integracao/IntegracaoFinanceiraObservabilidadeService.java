package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class IntegracaoFinanceiraObservabilidadeService {
    private final IntegracaoFinanceiraTentativaRepository tentativaRepository;
    private final IntegracaoFinanceiraRepository integracaoRepository;

    public IntegracaoFinanceiraObservabilidadeService(IntegracaoFinanceiraTentativaRepository tentativaRepository,
                                                       IntegracaoFinanceiraRepository integracaoRepository) {
        this.tentativaRepository = tentativaRepository; this.integracaoRepository = integracaoRepository;
    }

    public void sucesso(IntegracaoFinanceira integracao, int quantidade, long duracaoMs, Instant inicio, Instant fim) {
        tentativaRepository.save(IntegracaoFinanceiraTentativa.sucesso(integracao.getTenantId(), integracao.getId(),
                integracao.getProvedor(), quantidade, duracaoMs, inicio, fim));
    }

    public void falha(IntegracaoFinanceira integracao, int quantidade, long duracaoMs, Throwable erro, Instant inicio, Instant fim) {
        tentativaRepository.save(IntegracaoFinanceiraTentativa.falha(integracao.getTenantId(), integracao.getId(),
                integracao.getProvedor(), quantidade, duracaoMs, erro, inicio, fim));
    }

    public List<IntegracaoFinanceiraTentativa> listar(UUID tenantId, UUID integracaoId) {
        integracaoRepository.findByIdAndTenantId(integracaoId, tenantId).orElseThrow(() ->
                new RecursoNaoEncontradoException("Integracao financeira nao encontrada para o tenant informado"));
        return tentativaRepository.findTop50ByTenantIdAndIntegracaoIdOrderByIniciadoEmDesc(tenantId, integracaoId);
    }
}
