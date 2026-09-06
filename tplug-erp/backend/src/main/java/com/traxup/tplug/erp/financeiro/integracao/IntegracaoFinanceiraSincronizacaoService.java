package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.financeiro.ConciliacaoApplicationService;
import com.traxup.tplug.erp.financeiro.ConciliacaoLancamento;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class IntegracaoFinanceiraSincronizacaoService {
    private final IntegracaoFinanceiraRepository repository;
    private final ConciliacaoApplicationService conciliacaoService;
    private final AuditoriaApplicationService auditoria;

    public IntegracaoFinanceiraSincronizacaoService(IntegracaoFinanceiraRepository repository,
                                                     ConciliacaoApplicationService conciliacaoService,
                                                     AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.conciliacaoService = conciliacaoService;
        this.auditoria = auditoria;
    }

    @Transactional
    public ResultadoSincronizacao sincronizar(UUID tenantId, UUID usuarioId, UUID integracaoId,
                                               List<LancamentoExterno> lancamentos,
                                               String checkpoint, Instant sincronizadoEm) {
        IntegracaoFinanceira integracao = repository.findByIdAndTenantIdForUpdate(integracaoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Integracao financeira nao encontrada para o tenant informado"));
        if (!integracao.isAtivo()) {
            throw new RecursoConflitanteException("Integracao financeira inativa nao pode ser sincronizada");
        }

        List<ConciliacaoApplicationService.ImportacaoLancamento> itens = lancamentos == null ? null : lancamentos.stream()
                .map(item -> new ConciliacaoApplicationService.ImportacaoLancamento(
                        integracao.getProvedor(), item.referenciaExterna(), item.tipo(), item.valor(),
                        item.descricao(), item.ocorridoEm()))
                .toList();

        List<ConciliacaoLancamento> importados = conciliacaoService.importarLote(
                tenantId, usuarioId, integracao.getContaFinanceiraId(), itens);

        integracao.registrarSincronizacao(checkpoint, sincronizadoEm);
        repository.save(integracao);
        auditoria.registrar(tenantId, usuarioId, null, integracao.getFilialId(),
                "SINCRONIZAR", "INTEGRACAO_FINANCEIRA", integracao.getId(),
                "provedor=" + integracao.getProvedor() + ";sincronizadoEm=" + sincronizadoEm
                        + ";lancamentos=" + importados.size());

        return new ResultadoSincronizacao(integracao, importados);
    }

    public record LancamentoExterno(String referenciaExterna, String tipo, BigDecimal valor,
                                    String descricao, Instant ocorridoEm) {}

    public record ResultadoSincronizacao(IntegracaoFinanceira integracao,
                                         List<ConciliacaoLancamento> lancamentos) {}
}
