package com.traxup.tplug.erp.financeiro.pagamento;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PagamentoConfigApplicationService {
    private final FormaPagamentoRepository formaRepository;
    private final CondicaoPagamentoRepository condicaoRepository;
    private final CondicaoPagamentoParcelaRepository parcelaRepository;
    private final AuditoriaApplicationService auditoria;

    public PagamentoConfigApplicationService(FormaPagamentoRepository formaRepository,
                                             CondicaoPagamentoRepository condicaoRepository,
                                             CondicaoPagamentoParcelaRepository parcelaRepository,
                                             AuditoriaApplicationService auditoria) {
        this.formaRepository = formaRepository;
        this.condicaoRepository = condicaoRepository;
        this.parcelaRepository = parcelaRepository;
        this.auditoria = auditoria;
    }

    public List<FormaPagamento> listarFormas(UUID tenantId) {
        return formaRepository.findAllByTenantIdOrderByNomeAsc(tenantId);
    }

    public List<CondicaoPagamento> listarCondicoes(UUID tenantId) {
        return condicaoRepository.findAllByTenantIdOrderByNomeAsc(tenantId);
    }

    public CondicaoPagamento buscarCondicao(UUID tenantId, UUID id) {
        return condicaoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Condicao de pagamento nao encontrada para o tenant informado"));
    }

    public List<CondicaoPagamentoParcela> listarParcelas(UUID tenantId, UUID condicaoId) {
        buscarCondicao(tenantId, condicaoId);
        return parcelaRepository.findAllByTenantIdAndCondicaoPagamentoIdOrderByNumeroAsc(tenantId, condicaoId);
    }

    @Transactional
    public FormaPagamento criarForma(UUID tenantId, UUID usuarioId, String codigo, String nome) {
        String codigoNormalizado = obrigatorio(codigo, "Codigo").toUpperCase();
        if (formaRepository.existsByTenantIdAndCodigoIgnoreCase(tenantId, codigoNormalizado)) {
            throw new IllegalArgumentException("Codigo da forma de pagamento ja cadastrado para o tenant");
        }
        FormaPagamento forma = formaRepository.save(new FormaPagamento(tenantId, codigoNormalizado, obrigatorio(nome, "Nome")));
        auditoria.registrar(tenantId, usuarioId, null, null, "CRIAR", "FORMA_PAGAMENTO", forma.getId(), "codigo=" + codigoNormalizado);
        return forma;
    }

    @Transactional
    public CondicaoPagamento criarCondicao(UUID tenantId, UUID usuarioId, String codigo, String nome,
                                           List<ParcelaDefinicao> parcelas) {
        String codigoNormalizado = obrigatorio(codigo, "Codigo").toUpperCase();
        if (condicaoRepository.existsByTenantIdAndCodigoIgnoreCase(tenantId, codigoNormalizado)) {
            throw new IllegalArgumentException("Codigo da condicao de pagamento ja cadastrado para o tenant");
        }
        validarParcelas(parcelas);
        CondicaoPagamento condicao = condicaoRepository.save(new CondicaoPagamento(tenantId, codigoNormalizado, obrigatorio(nome, "Nome")));
        for (ParcelaDefinicao parcela : parcelas) {
            parcelaRepository.save(new CondicaoPagamentoParcela(tenantId, condicao.getId(), parcela.numero(), parcela.dias(), parcela.percentual()));
        }
        auditoria.registrar(tenantId, usuarioId, null, null, "CRIAR", "CONDICAO_PAGAMENTO", condicao.getId(), "parcelas=" + parcelas.size());
        return condicao;
    }

    @Transactional
    public void desativarForma(UUID tenantId, UUID usuarioId, UUID id) {
        FormaPagamento forma = formaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Forma de pagamento nao encontrada para o tenant informado"));
        forma.desativar();
        formaRepository.save(forma);
        auditoria.registrar(tenantId, usuarioId, null, null, "DESATIVAR", "FORMA_PAGAMENTO", id, null);
    }

    @Transactional
    public void desativarCondicao(UUID tenantId, UUID usuarioId, UUID id) {
        CondicaoPagamento condicao = buscarCondicao(tenantId, id);
        condicao.desativar();
        condicaoRepository.save(condicao);
        auditoria.registrar(tenantId, usuarioId, null, null, "DESATIVAR", "CONDICAO_PAGAMENTO", id, null);
    }

    private void validarParcelas(List<ParcelaDefinicao> parcelas) {
        if (parcelas == null || parcelas.isEmpty()) throw new IllegalArgumentException("Condicao de pagamento precisa possuir ao menos uma parcela");
        BigDecimal total = BigDecimal.ZERO;
        int esperado = 1;
        for (ParcelaDefinicao parcela : parcelas.stream().sorted((a, b) -> Integer.compare(a.numero(), b.numero())).toList()) {
            if (parcela.numero() != esperado++) throw new IllegalArgumentException("Numeracao das parcelas deve ser sequencial a partir de 1");
            if (parcela.dias() < 0) throw new IllegalArgumentException("Dias da parcela nao pode ser negativo");
            if (parcela.percentual() == null || parcela.percentual().signum() <= 0) throw new IllegalArgumentException("Percentual da parcela deve ser maior que zero");
            total = total.add(parcela.percentual());
        }
        if (total.compareTo(new BigDecimal("100.0000")) != 0) throw new IllegalArgumentException("Soma dos percentuais das parcelas deve ser 100");
    }

    private String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " e obrigatorio");
        return valor.trim();
    }

    public record ParcelaDefinicao(int numero, int dias, BigDecimal percentual) {}
}
