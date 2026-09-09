package com.traxup.tplug.erp.pdv;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.venda.PedidoVenda;
import com.traxup.tplug.erp.venda.PedidoVendaApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PdvVendaRascunhoApplicationService {
    private final PdvVendaSincronizacaoApplicationService sincronizacaoService;
    private final PdvVendaSincronizacaoRepository sincronizacaoRepository;
    private final PedidoVendaApplicationService pedidoVendaService;
    private final AuditoriaApplicationService auditoria;

    public PdvVendaRascunhoApplicationService(PdvVendaSincronizacaoApplicationService sincronizacaoService,
                                              PdvVendaSincronizacaoRepository sincronizacaoRepository,
                                              PedidoVendaApplicationService pedidoVendaService,
                                              AuditoriaApplicationService auditoria) {
        this.sincronizacaoService = sincronizacaoService;
        this.sincronizacaoRepository = sincronizacaoRepository;
        this.pedidoVendaService = pedidoVendaService;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado criarOuObter(UUID tenantId, UUID usuarioId, UUID terminalId, UUID operacaoLocalId,
                                  Long numeroLocal, String checksum, Instant ocorridoEm,
                                  UUID clienteId, String observacao) {
        var ack = sincronizacaoService.sincronizar(
                tenantId, usuarioId, terminalId, operacaoLocalId, numeroLocal, checksum, ocorridoEm);
        PdvVendaSincronizacao sincronizacao = ack.sincronizacao();

        if (sincronizacao.possuiPedidoVenda()) {
            PedidoVenda existente = pedidoVendaService.buscar(tenantId, sincronizacao.getPedidoVendaId());
            return new Resultado(sincronizacao, existente, true);
        }

        String numeroPedido = "PDV-" + sincronizacao.getId();
        PedidoVenda pedido = pedidoVendaService.criar(
                tenantId,
                sincronizacao.getFilialId(),
                clienteId,
                numeroPedido,
                observacao,
                usuarioId);

        sincronizacao.vincularPedidoVenda(pedido.getId());
        sincronizacaoRepository.save(sincronizacao);

        auditoria.registrar(
                tenantId,
                usuarioId,
                null,
                sincronizacao.getFilialId(),
                "VINCULAR_PEDIDO",
                "PDV_VENDA",
                sincronizacao.getId(),
                "pedidoVendaId=" + pedido.getId() + ";terminalId=" + terminalId + ";numeroLocal=" + numeroLocal);

        return new Resultado(sincronizacao, pedido, false);
    }

    public record Resultado(PdvVendaSincronizacao sincronizacao, PedidoVenda pedidoVenda, boolean repetida) {}
}
