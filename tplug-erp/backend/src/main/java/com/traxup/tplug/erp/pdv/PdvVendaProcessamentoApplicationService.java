package com.traxup.tplug.erp.pdv;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.venda.PedidoVenda;
import com.traxup.tplug.erp.venda.PedidoVendaApplicationService;
import com.traxup.tplug.erp.venda.PedidoVendaItem;
import com.traxup.tplug.erp.venda.PedidoVendaItemApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PdvVendaProcessamentoApplicationService {
    private final PdvVendaSincronizacaoApplicationService sincronizacaoService;
    private final PdvVendaSincronizacaoRepository sincronizacaoRepository;
    private final PedidoVendaApplicationService pedidoService;
    private final PedidoVendaItemApplicationService itemService;
    private final AuditoriaApplicationService auditoria;

    public PdvVendaProcessamentoApplicationService(PdvVendaSincronizacaoApplicationService sincronizacaoService,
                                                   PdvVendaSincronizacaoRepository sincronizacaoRepository,
                                                   PedidoVendaApplicationService pedidoService,
                                                   PedidoVendaItemApplicationService itemService,
                                                   AuditoriaApplicationService auditoria) {
        this.sincronizacaoService = sincronizacaoService;
        this.sincronizacaoRepository = sincronizacaoRepository;
        this.pedidoService = pedidoService;
        this.itemService = itemService;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado processar(UUID tenantId, UUID usuarioId, UUID terminalId, UUID operacaoLocalId,
                               Long numeroLocal, String checksum, Instant ocorridoEm, UUID clienteId,
                               UUID formaPagamentoId, UUID condicaoPagamentoId, String observacao,
                               List<ItemComando> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Venda do PDV precisa possuir ao menos um item");
        }

        var ack = sincronizacaoService.sincronizar(
                tenantId, usuarioId, terminalId, operacaoLocalId, numeroLocal, checksum, ocorridoEm);
        PdvVendaSincronizacao sincronizacao = ack.sincronizacao();

        if (sincronizacao.isProcessada()) {
            PedidoVenda pedidoExistente = pedidoService.buscar(tenantId, sincronizacao.getPedidoVendaId());
            return new Resultado(sincronizacao, pedidoExistente, true);
        }

        String numeroPedido = "PDV-" + sincronizacao.getId().toString().replace("-", "");
        PedidoVenda pedido = pedidoService.criar(
                tenantId, sincronizacao.getFilialId(), clienteId, numeroPedido, observacao, usuarioId);

        for (ItemComando comando : itens) {
            PedidoVendaItem item = itemService.adicionar(
                    tenantId, usuarioId, pedido.getId(), comando.produtoId(), comando.gradeId(),
                    comando.quantidade(), comando.precoUnitario());
            if (comando.descontoValor() != null && comando.descontoValor().signum() > 0) {
                itemService.aplicarDesconto(
                        tenantId, usuarioId, pedido.getId(), item.getId(), comando.descontoValor());
            }
        }

        pedidoService.configurarPagamento(
                tenantId, usuarioId, pedido.getId(), formaPagamentoId, condicaoPagamentoId);
        pedidoService.abrir(tenantId, usuarioId, pedido.getId());
        PedidoVenda faturado = pedidoService.faturar(tenantId, usuarioId, pedido.getId());

        sincronizacao.marcarProcessada(faturado.getId());
        sincronizacaoRepository.save(sincronizacao);
        auditoria.registrar(
                tenantId, usuarioId, null, sincronizacao.getFilialId(),
                "PROCESSAR", "PDV_VENDA", sincronizacao.getId(),
                "pedidoVendaId=" + faturado.getId() + ";terminalId=" + terminalId + ";numeroLocal=" + numeroLocal);

        return new Resultado(sincronizacao, faturado, false);
    }

    public record ItemComando(UUID produtoId, UUID gradeId, BigDecimal quantidade,
                              BigDecimal precoUnitario, BigDecimal descontoValor) {}

    public record Resultado(PdvVendaSincronizacao sincronizacao, PedidoVenda pedidoVenda, boolean repetida) {}
}
