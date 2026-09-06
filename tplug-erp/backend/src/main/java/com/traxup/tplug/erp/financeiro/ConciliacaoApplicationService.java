package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConciliacaoApplicationService {
    private final ConciliacaoLancamentoRepository repository;
    private final ContaFinanceiraApplicationService contaFinanceiraService;
    private final ContaFinanceiraMovimentoRepository movimentoRepository;
    private final AuditoriaApplicationService auditoria;

    public ConciliacaoApplicationService(ConciliacaoLancamentoRepository repository,
                                         ContaFinanceiraApplicationService contaFinanceiraService,
                                         ContaFinanceiraMovimentoRepository movimentoRepository,
                                         AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.contaFinanceiraService = contaFinanceiraService;
        this.movimentoRepository = movimentoRepository;
        this.auditoria = auditoria;
    }

    public List<ConciliacaoLancamento> listar(UUID tenantId, UUID contaId) {
        contaFinanceiraService.buscar(tenantId, contaId);
        return repository.findAllByTenantIdAndContaFinanceiraIdOrderByOcorridoEmDesc(tenantId, contaId);
    }

    @Transactional
    public ConciliacaoLancamento importar(UUID tenantId, UUID usuarioId, UUID contaId, String origem,
                                           String referenciaExterna, String tipo, BigDecimal valor,
                                           String descricao, Instant ocorridoEm) {
        ContaFinanceira conta = contaFinanceiraService.buscar(tenantId, contaId);
        String origemNormalizada = obrigatorio(origem, "Origem").toUpperCase(Locale.ROOT);
        String referenciaNormalizada = obrigatorio(referenciaExterna, "Referencia externa");
        String tipoNormalizado = normalizarTipo(tipo);
        String descricaoNormalizada = obrigatorio(descricao, "Descricao");
        if (valor == null || valor.signum() <= 0) throw new IllegalArgumentException("Valor deve ser maior que zero");
        if (ocorridoEm == null) throw new IllegalArgumentException("Data/hora do lancamento e obrigatoria");
        if (repository.existsByTenantIdAndContaFinanceiraIdAndOrigemAndReferenciaExterna(
                tenantId, contaId, origemNormalizada, referenciaNormalizada)) {
            throw new RecursoConflitanteException("Lancamento externo ja importado para esta conta");
        }
        ConciliacaoLancamento lancamento = repository.save(new ConciliacaoLancamento(
                tenantId, conta.getFilialId(), contaId, origemNormalizada, referenciaNormalizada,
                tipoNormalizado, valor, descricaoNormalizada, ocorridoEm, usuarioId));
        auditoria.registrar(tenantId, usuarioId, null, conta.getFilialId(),
                "IMPORTAR", "CONCILIACAO_FINANCEIRA", lancamento.getId(),
                "origem=" + origemNormalizada + ";referencia=" + referenciaNormalizada + ";valor=" + valor);
        return lancamento;
    }

    @Transactional
    public ConciliacaoLancamento conciliar(UUID tenantId, UUID usuarioId, UUID lancamentoId, UUID movimentoId) {
        ConciliacaoLancamento lancamento = repository.findByIdAndTenantId(lancamentoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lancamento de conciliacao nao encontrado para o tenant informado"));
        ContaFinanceiraMovimento movimento = movimentoRepository.findByIdAndTenantId(movimentoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Movimento financeiro nao encontrado para o tenant informado"));
        if (!lancamento.getContaFinanceiraId().equals(movimento.getContaFinanceiraId())) {
            throw new IllegalArgumentException("Lancamento e movimento devem pertencer a mesma conta financeira");
        }
        if (!lancamento.getFilialId().equals(movimento.getFilialId())) {
            throw new IllegalArgumentException("Lancamento e movimento devem pertencer a mesma filial");
        }
        if (!lancamento.getTipo().equals(movimento.getTipo())) {
            throw new IllegalArgumentException("Tipo do lancamento externo difere do movimento financeiro");
        }
        if (lancamento.getValor().compareTo(movimento.getValor()) != 0) {
            throw new IllegalArgumentException("Valor do lancamento externo difere do movimento financeiro");
        }
        lancamento.conciliar(movimentoId);
        repository.save(lancamento);
        auditoria.registrar(tenantId, usuarioId, null, lancamento.getFilialId(),
                "CONCILIAR", "CONCILIACAO_FINANCEIRA", lancamento.getId(), "movimentoId=" + movimentoId);
        return lancamento;
    }

    private String normalizarTipo(String tipo) {
        String valor = obrigatorio(tipo, "Tipo").toUpperCase(Locale.ROOT);
        if (!("ENTRADA".equals(valor) || "SAIDA".equals(valor))) {
            throw new IllegalArgumentException("Tipo deve ser ENTRADA ou SAIDA");
        }
        return valor;
    }

    private String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " e obrigatorio");
        return valor.trim();
    }
}
