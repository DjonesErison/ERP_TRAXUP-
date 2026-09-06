package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class IntegracaoFinanceiraObservabilidadeService {
    private final IntegracaoFinanceiraTentativaRepository tentativaRepository;
    private final IntegracaoFinanceiraRepository integracaoRepository;

    public IntegracaoFinanceiraObservabilidadeService(IntegracaoFinanceiraTentativaRepository tentativaRepository,
                                                       IntegracaoFinanceiraRepository integracaoRepository) {
        this.tentativaRepository = tentativaRepository;
        this.integracaoRepository = integracaoRepository;
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
        return listar(tenantId, integracaoId, null, null, null);
    }

    public List<IntegracaoFinanceiraTentativa> listar(UUID tenantId, UUID integracaoId,
                                                       String status, Instant inicio, Instant fim) {
        String statusNormalizado = normalizarStatus(status);
        validarPeriodo(inicio, fim);
        validarIntegracao(tenantId, integracaoId);
        return tentativaRepository.filtrar(tenantId, integracaoId, statusNormalizado, inicio, fim, PageRequest.of(0, 50));
    }

    public ResumoSaude resumirSaude(UUID tenantId, UUID integracaoId) {
        validarIntegracao(tenantId, integracaoId);
        List<IntegracaoFinanceiraTentativa> tentativas =
                tentativaRepository.findTop50ByTenantIdAndIntegracaoIdOrderByIniciadoEmDesc(tenantId, integracaoId);
        if (tentativas.isEmpty()) {
            return new ResumoSaude(integracaoId, "SEM_EXECUCAO", null, null, 0, 0, 0, 0, null);
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
        int sucessos = (int) tentativas.stream().filter(t -> "SUCESSO".equals(t.getStatus())).count();
        int falhas = (int) tentativas.stream().filter(t -> "FALHA".equals(t.getStatus())).count();
        long somaDuracao = tentativas.stream().mapToLong(IntegracaoFinanceiraTentativa::getDuracaoMs).sum();
        long duracaoMediaMs = somaDuracao / tentativas.size();
        String status = falhasConsecutivas == 0 ? "SAUDAVEL" : "ATENCAO";
        return new ResumoSaude(integracaoId, status, ultimaTentativaEm, ultimoSucessoEm,
                falhasConsecutivas, tentativas.size(), sucessos, falhas, duracaoMediaMs);
    }

    private String normalizarStatus(String status) {
        if (status == null || status.isBlank()) return null;
        String normalizado = status.trim().toUpperCase(Locale.ROOT);
        if (!"SUCESSO".equals(normalizado) && !"FALHA".equals(normalizado)) {
            throw new IllegalArgumentException("Status de tentativa invalido");
        }
        return normalizado;
    }

    private void validarPeriodo(Instant inicio, Instant fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Inicio nao pode ser posterior ao fim");
        }
    }

    private void validarIntegracao(UUID tenantId, UUID integracaoId) {
        integracaoRepository.findByIdAndTenantId(integracaoId, tenantId).orElseThrow(() ->
                new RecursoNaoEncontradoException("Integracao financeira nao encontrada para o tenant informado"));
    }

    public record ResumoSaude(UUID integracaoId, String status, Instant ultimaTentativaEm,
                              Instant ultimoSucessoEm, int falhasConsecutivas, int tentativasConsideradas,
                              int sucessos, int falhas, Long duracaoMediaMs) {
        public ResumoSaude(UUID integracaoId, String status, Instant ultimaTentativaEm,
                           Instant ultimoSucessoEm, int falhasConsecutivas, int tentativasConsideradas) {
            this(integracaoId, status, ultimaTentativaEm, ultimoSucessoEm,
                    falhasConsecutivas, tentativasConsideradas, 0, 0, null);
        }
    }
}
