package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamento;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcela;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcelaRepository;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PedidoVendaPreviaFinanceiraService {
    private final PedidoVendaRepository pedidoRepository;
    private final PedidoVendaItemRepository itemRepository;
    private final CondicaoPagamentoRepository condicaoRepository;
    private final CondicaoPagamentoParcelaRepository parcelaRepository;

    public PedidoVendaPreviaFinanceiraService(PedidoVendaRepository pedidoRepository,
                                              PedidoVendaItemRepository itemRepository,
                                              CondicaoPagamentoRepository condicaoRepository,
                                              CondicaoPagamentoParcelaRepository parcelaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemRepository = itemRepository;
        this.condicaoRepository = condicaoRepository;
        this.parcelaRepository = parcelaRepository;
    }

    public PreviaFinanceira prever(UUID tenantId, UUID pedidoId) {
        PedidoVenda pedido = pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de venda nao encontrado para o tenant informado"));
        List<PedidoVendaItem> itens = itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(tenantId, pedidoId);
        if (itens.isEmpty()) throw new IllegalArgumentException("Pedido de venda precisa possuir itens para gerar previa financeira");

        BigDecimal totalLiquido = itens.stream().map(PedidoVendaItem::getTotalItem).reduce(BigDecimal.ZERO, BigDecimal::add);
        CondicaoPagamento condicao = null;
        List<CondicaoPagamentoParcela> parcelas = List.of();
        if (pedido.getCondicaoPagamentoId() != null) {
            condicao = condicaoRepository.findByIdAndTenantId(pedido.getCondicaoPagamentoId(), tenantId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Condicao de pagamento nao encontrada para o tenant informado"));
            parcelas = parcelaRepository.findAllByTenantIdAndCondicaoPagamentoIdOrderByNumeroAsc(
                    tenantId, pedido.getCondicaoPagamentoId());
        }

        PedidoVendaPlanoFinanceiroCalculadora.Plano plano = PedidoVendaPlanoFinanceiroCalculadora
                .calcular(totalLiquido, condicao, parcelas, LocalDate.now());
        return new PreviaFinanceira(plano.totalLiquido(), plano.desconto(), plano.juros(), plano.entrada(),
                plano.totalFinanceiro(), plano.saldoParcelar(),
                plano.titulos().stream()
                        .map(t -> new ParcelaPrevista(t.tipo(), t.numero(), t.vencimento(), t.valor()))
                        .toList());
    }

    public record PreviaFinanceira(BigDecimal totalLiquido, BigDecimal desconto, BigDecimal juros, BigDecimal entrada,
                                   BigDecimal totalFinanceiro, BigDecimal saldoParcelar,
                                   List<ParcelaPrevista> titulos) {}
    public record ParcelaPrevista(String tipo, int numero, LocalDate vencimento, BigDecimal valor) {}
}
