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
        validarIntegracao(tenantId, integracaoId);
        return tentativaRepository.findTop50ByTenantIdAndIntegracaoIdOrderByIniciadoEmDesc(tenantId, integracaoId);
    }

    public ResumoSaude resumirSaude(UUID tenantId, UUID integracaoId) {
        validarIntegracao(tenantId, integracaoId);
        List<IntegracaoFinanceiraTentativa> tentativas =
                tentativaRepository.findTop50ByTenantIdAndIntegracaoIdOrderByIniciadoEmDesc(tenantId, integracaoId);
        if (tentativas.isEmpty()) {
            return new ResumoSaude(integracaoId, "SEM_EXECUCAO", null, null, 0, 0);
        }
        Instant ultimaTentativaEm = tentativas.get(0).getFinalizadoEm();
        Instant ultimoSucessoEm = tentativas.stream()
                .filter(t -> "SUCESSO".equals(t.getStatus()))
                .map(IntegracaoFinanceiraTentativa::getFinalizadoEm)
                .findFirst().orElse(null);
        int falhasConsecutivas = 0;
        for (IntegracaoFinanceiraTentativa tentativa : tentativas) {
            if (!"FALHA".equals(tentativa.getStatus())) break;
            falhasConsecutivas++;
        }
        String status = falhasConsecutivas == 0 ? "SAUDAVEL" : "ATENCAO";
        return new ResumoSaude(integracaoId, status, ultimaTentativaEm, ultimoSucessoEm,
                falhasConsecutivas, tentativas.size());
    }

    private void validarIntegracao(UUID tenantId, UUID integracaoId) {
        integracaoRepository.findByIdAndTenantId(integracaoId, tenantId).orElseThrow(() ->
                new RecursoNaoEncontradoException("Integracao financeira nao encontrada para o tenant informado"));
    }

    public record ResumoSaude(UUID integracaoId, String status, Instant ultimaTentativaEm,
                              Instant ultimoSucessoEm, int falhasConsecutivas, int tentativasConsideradas) {}
}
