package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.financeiro.pagamento.AjusteComercialCalculadora;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamento;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcela;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcelaRepository;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
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
        List<PedidoVendaItem> itens = itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAsc(tenantId, pedidoId);
        if (itens.isEmpty()) throw new IllegalArgumentException("Pedido de venda precisa possuir itens para gerar previa financeira");

        BigDecimal totalLiquido = itens.stream().map(PedidoVendaItem::getTotalItem).reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDate hoje = LocalDate.now();
        if (pedido.getCondicaoPagamentoId() == null) {
            BigDecimal total = totalLiquido.setScale(4, RoundingMode.HALF_UP);
            return new PreviaFinanceira(total, BigDecimal.ZERO.setScale(4), BigDecimal.ZERO.setScale(4),
                    BigDecimal.ZERO.setScale(4), total, total,
                    List.of(new ParcelaPrevista("PARCELA", 1, hoje, total)));
        }

        CondicaoPagamento condicao = condicaoRepository.findByIdAndTenantId(pedido.getCondicaoPagamentoId(), tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Condicao de pagamento nao encontrada para o tenant informado"));
        AjusteComercialCalculadora.Resultado ajustes = AjusteComercialCalculadora.calcular(totalLiquido, condicao);
        List<ParcelaPrevista> titulos = new ArrayList<>();
        if (ajustes.entrada().signum() > 0) {
            titulos.add(new ParcelaPrevista("ENTRADA", 0, hoje, ajustes.entrada()));
        }
        if (ajustes.saldoParcelar().signum() > 0) {
            List<CondicaoPagamentoParcela> parcelas = parcelaRepository
                    .findAllByTenantIdAndCondicaoPagamentoIdOrderByNumeroAsc(tenantId, pedido.getCondicaoPagamentoId());
            if (parcelas.isEmpty()) throw new IllegalArgumentException("Condicao de pagamento precisa possuir parcelas");
            BigDecimal acumulado = BigDecimal.ZERO;
            for (int i = 0; i < parcelas.size(); i++) {
                CondicaoPagamentoParcela parcela = parcelas.get(i);
                BigDecimal valor = i == parcelas.size() - 1
                        ? ajustes.saldoParcelar().subtract(acumulado)
                        : ajustes.saldoParcelar().multiply(parcela.getPercentual())
                        .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                acumulado = acumulado.add(valor);
                titulos.add(new ParcelaPrevista("PARCELA", parcela.getNumero(), hoje.plusDays(parcela.getDias()), valor));
            }
        }
        return new PreviaFinanceira(ajustes.totalLiquido(), ajustes.desconto(), ajustes.juros(), ajustes.entrada(),
                ajustes.totalFinanceiro(), ajustes.saldoParcelar(), List.copyOf(titulos));
    }

    public record PreviaFinanceira(BigDecimal totalLiquido, BigDecimal desconto, BigDecimal juros, BigDecimal entrada,
                                   BigDecimal totalFinanceiro, BigDecimal saldoParcelar,
                                   List<ParcelaPrevista> titulos) {}
    public record ParcelaPrevista(String tipo, int numero, LocalDate vencimento, BigDecimal valor) {}
}
