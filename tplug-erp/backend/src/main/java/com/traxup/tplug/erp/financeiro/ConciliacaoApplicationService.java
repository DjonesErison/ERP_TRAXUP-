package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConciliacaoApplicationService {
    private static final Duration JANELA_SUGESTAO = Duration.ofDays(3);
    private static final int LIMITE_PADRAO_LISTAGEM = 100;
    private static final int LIMITE_MAXIMO_LISTAGEM = 500;
    private static final Set<String> TIPOS = Set.of("ENTRADA", "SAIDA");
    private static final Set<String> NATUREZAS = Set.of("NORMAL", "TAXA", "ANTECIPACAO", "ESTORNO", "CHARGEBACK");
    private static final Set<String> STATUS = Set.of("PENDENTE", "CONCILIADO");
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
        return listar(tenantId, contaId, null, null, null, null, null, null);
    }

    public List<ConciliacaoLancamento> listar(UUID tenantId, UUID contaId, String origem, String natureza,
                                              String status, Instant inicio, Instant fim) {
        return listar(tenantId, contaId, origem, natureza, status, null, inicio, fim);
    }

    public List<ConciliacaoLancamento> listar(UUID tenantId, UUID contaId, String origem, String natureza,
                                              String status, String tipo, Instant inicio, Instant fim) {
        contaFinanceiraService.buscar(tenantId, contaId);
        validarPeriodo(inicio, fim);
        return repository.filtrar(tenantId, contaId, opcionalUpper(origem),
                opcionalPermitido(natureza, NATUREZAS, "Natureza"),
                opcionalPermitido(status, STATUS, "Status"),
                opcionalPermitido(tipo, TIPOS, "Tipo"), inicio, fim);
    }

    public List<ConciliacaoLancamento> listar(UUID tenantId, UUID contaId, String origem, String natureza,
                                              String status, String tipo, Instant inicio, Instant fim, Integer limite) {
        contaFinanceiraService.buscar(tenantId, contaId);
        validarPeriodo(inicio, fim);
        int limiteValidado = validarLimite(limite);
        return repository.filtrar(tenantId, contaId, opcionalUpper(origem),
                opcionalPermitido(natureza, NATUREZAS, "Natureza"),
                opcionalPermitido(status, STATUS, "Status"),
                opcionalPermitido(tipo, TIPOS, "Tipo"), inicio, fim, PageRequest.of(0, limiteValidado));
    }

    public ConciliacaoResumoProjection resumir(UUID tenantId, UUID contaId) {
        contaFinanceiraService.buscar(tenantId, contaId);
        return repository.resumir(tenantId, contaId);
    }

    public ConciliacaoResumoProjection resumir(UUID tenantId, UUID contaId, String origem, String natureza,
                                                String status, Instant inicio, Instant fim) {
        return resumir(tenantId, contaId, origem, natureza, status, null, inicio, fim);
    }

    public ConciliacaoResumoProjection resumir(UUID tenantId, UUID contaId, String origem, String natureza,
                                                String status, String tipo, Instant inicio, Instant fim) {
        contaFinanceiraService.buscar(tenantId, contaId);
        validarPeriodo(inicio, fim);
        return repository.resumirFiltrado(tenantId, contaId, opcionalUpper(origem),
                opcionalPermitido(natureza, NATUREZAS, "Natureza"),
                opcionalPermitido(status, STATUS, "Status"),
                opcionalPermitido(tipo, TIPOS, "Tipo"), inicio, fim);
    }

    public List<ContaFinanceiraMovimento> sugerirMovimentos(UUID tenantId, UUID lancamentoId) {
        ConciliacaoLancamento lancamento = buscarLancamento(tenantId, lancamentoId);
        if (!"PENDENTE".equals(lancamento.getStatus())) {
            throw new RecursoConflitanteException("Somente lancamento PENDENTE pode receber sugestoes de conciliacao");
        }
        Instant inicio = lancamento.getOcorridoEm().minus(JANELA_SUGESTAO);
        Instant fim = lancamento.getOcorridoEm().plus(JANELA_SUGESTAO);
        return movimentoRepository.findCandidatosDisponiveis(
                tenantId, lancamento.getContaFinanceiraId(), lancamento.getFilialId(), lancamento.getTipo(),
                lancamento.getValor(), inicio, fim);
    }

    @Transactional
    public List<ConciliacaoLancamento> importarLote(UUID tenantId, UUID usuarioId, UUID contaId,
                                                     List<ImportacaoLancamento> lancamentos) {
        if (lancamentos == null || lancamentos.isEmpty()) throw new RegraNegocioException("Lote deve possuir lancamentos");
        if (lancamentos.size() > 500) throw new RegraNegocioException("Lote excede o limite de 500 lancamentos");
        List<ConciliacaoLancamento> resultado = new ArrayList<>(lancamentos.size());
        for (ImportacaoLancamento item : lancamentos) {
            resultado.add(importar(tenantId, usuarioId, contaId, item.origem(), item.referenciaExterna(),
                    item.tipo(), item.valor(), item.descricao(), item.ocorridoEm()));
        }
        return resultado;
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
        if (valor == null || valor.signum() <= 0) throw new RegraNegocioException("Valor deve ser maior que zero");
        if (ocorridoEm == null) throw new RegraNegocioException("Data/hora do lancamento e obrigatoria");

        repository.bloquearContaParaImportacao(tenantId, contaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta financeira nao encontrada para o tenant informado"));

        var existente = repository.findByTenantIdAndContaFinanceiraIdAndOrigemAndReferenciaExterna(
                tenantId, contaId, origemNormalizada, referenciaNormalizada);
        if (existente.isPresent()) {
            if (mesmoConteudo(existente.get(), tipoNormalizado, valor, descricaoNormalizada, ocorridoEm)) {
                return existente.get();
            }
            throw new RecursoConflitanteException(
                    "Referencia externa ja importada para esta conta com conteudo diferente");
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
    public ConciliacaoLancamento classificar(UUID tenantId, UUID usuarioId, UUID lancamentoId, String natureza) {
        ConciliacaoLancamento lancamento = repository.findByIdAndTenantIdForUpdate(lancamentoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lancamento de conciliacao nao encontrado para o tenant informado"));
        String naturezaNormalizada = obrigatorio(natureza, "Natureza").toUpperCase(Locale.ROOT);
        lancamento.classificar(naturezaNormalizada);
        repository.save(lancamento);
        auditoria.registrar(tenantId, usuarioId, null, lancamento.getFilialId(),
                "CLASSIFICAR", "CONCILIACAO_FINANCEIRA", lancamento.getId(),
                "natureza=" + naturezaNormalizada);
        return lancamento;
    }

    @Transactional
    public ConciliacaoLancamento conciliar(UUID tenantId, UUID usuarioId, UUID lancamentoId, UUID movimentoId) {
        ConciliacaoLancamento lancamento = repository.findByIdAndTenantIdForUpdate(lancamentoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lancamento de conciliacao nao encontrado para o tenant informado"));
        ContaFinanceiraMovimento movimento = movimentoRepository.findByIdAndTenantIdForUpdate(movimentoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Movimento financeiro nao encontrado para o tenant informado"));
        if (repository.existsByTenantIdAndMovimentoIdAndIdNot(tenantId, movimentoId, lancamentoId)) {
            throw new RecursoConflitanteException("Movimento financeiro ja utilizado em outra conciliacao");
        }
        if (!lancamento.getContaFinanceiraId().equals(movimento.getContaFinanceiraId())) {
            throw new RegraNegocioException("Lancamento e movimento devem pertencer a mesma conta financeira");
        }
        if (!lancamento.getFilialId().equals(movimento.getFilialId())) {
            throw new RegraNegocioException("Lancamento e movimento devem pertencer a mesma filial");
        }
        if (!lancamento.getTipo().equals(movimento.getTipo())) {
            throw new RegraNegocioException("Tipo do lancamento externo difere do movimento financeiro");
        }
        if (lancamento.getValor().compareTo(movimento.getValor()) != 0) {
            throw new RegraNegocioException("Valor do lancamento externo difere do movimento financeiro");
        }
        lancamento.conciliar(movimentoId);
        repository.save(lancamento);
        auditoria.registrar(tenantId, usuarioId, null, lancamento.getFilialId(),
                "CONCILIAR", "CONCILIACAO_FINANCEIRA", lancamento.getId(), "movimentoId=" + movimentoId);
        return lancamento;
    }

    private ConciliacaoLancamento buscarLancamento(UUID tenantId, UUID lancamentoId) {
        return repository.findByIdAndTenantId(lancamentoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lancamento de conciliacao nao encontrado para o tenant informado"));
    }

    private boolean mesmoConteudo(ConciliacaoLancamento existente, String tipo, BigDecimal valor,
                                  String descricao, Instant ocorridoEm) {
        return existente.getTipo().equals(tipo)
                && existente.getValor().compareTo(valor) == 0
                && existente.getDescricao().equals(descricao)
                && Objects.equals(existente.getOcorridoEm(), ocorridoEm);
    }

    private String normalizarTipo(String tipo) {
        String valor = obrigatorio(tipo, "Tipo").toUpperCase(Locale.ROOT);
        if (!TIPOS.contains(valor)) {
            throw new RegraNegocioException("Tipo deve ser ENTRADA ou SAIDA");
        }
        return valor;
    }

    private void validarPeriodo(Instant inicio, Instant fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new RegraNegocioException("Periodo inicial nao pode ser posterior ao periodo final");
        }
    }

    private int validarLimite(Integer limite) {
        int valor = limite == null ? LIMITE_PADRAO_LISTAGEM : limite;
        if (valor < 1 || valor > LIMITE_MAXIMO_LISTAGEM) {
            throw new RegraNegocioException("Limite da conciliacao deve estar entre 1 e 500");
        }
        return valor;
    }

    private String opcionalPermitido(String valor, Set<String> permitidos, String campo) {
        String normalizado = opcionalUpper(valor);
        if (normalizado != null && !permitidos.contains(normalizado)) {
            throw new RegraNegocioException(campo + " de conciliacao invalido");
        }
        return normalizado;
    }

    private String opcionalUpper(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase(Locale.ROOT);
    }

    private String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new RegraNegocioException(campo + " e obrigatorio");
        return valor.trim();
    }

    public record ImportacaoLancamento(String origem, String referenciaExterna, String tipo, BigDecimal valor,
                                       String descricao, Instant ocorridoEm) {}
}
