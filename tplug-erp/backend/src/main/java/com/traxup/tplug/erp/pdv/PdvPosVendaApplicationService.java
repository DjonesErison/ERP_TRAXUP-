package com.traxup.tplug.erp.pdv;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.venda.PedidoVenda;
import com.traxup.tplug.erp.venda.PedidoVendaApplicationService;
import com.traxup.tplug.erp.venda.PedidoVendaConsultaRecenteService;
import com.traxup.tplug.erp.venda.PedidoVendaDetalheConsultaService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PdvPosVendaApplicationService {
    private final PedidoVendaConsultaRecenteService consultaRecenteService;
    private final PedidoVendaDetalheConsultaService detalheConsultaService;
    private final PedidoVendaApplicationService pedidoVendaService;
    private final AuditoriaApplicationService auditoria;

    public PdvPosVendaApplicationService(PedidoVendaConsultaRecenteService consultaRecenteService,
                                         PedidoVendaDetalheConsultaService detalheConsultaService,
                                         PedidoVendaApplicationService pedidoVendaService,
                                         AuditoriaApplicationService auditoria) {
        this.consultaRecenteService = consultaRecenteService;
        this.detalheConsultaService = detalheConsultaService;
        this.pedidoVendaService = pedidoVendaService;
        this.auditoria = auditoria;
    }

    public Page<PedidoVenda> listarRecentes(UUID tenantId, int pagina, int tamanho, UUID filialId, String status) {
        return consultaRecenteService.listar(tenantId, pagina, tamanho, filialId, null, null, null, status, null, null);
    }

    public PedidoVendaDetalheConsultaService.Detalhe detalhe(UUID tenantId, UUID pedidoId) {
        return detalheConsultaService.consultar(tenantId, pedidoId);
    }

    @Transactional
    public Comprovante segundaVia(UUID tenantId, UUID usuarioId, UUID pedidoId) {
        var detalhe = detalheConsultaService.consultar(tenantId, pedidoId);
        PedidoVenda pedido = detalhe.pedido();
        if (!"FATURADO".equals(pedido.getStatus())) {
            throw new IllegalArgumentException("Segunda via so pode ser emitida para venda FATURADA");
        }
        Instant emitidoEm = Instant.now();
        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(),
                "REIMPRIMIR", "PEDIDO_VENDA", pedido.getId(), "tipo=SEGUNDA_VIA");
        return new Comprovante(detalhe, emitidoEm);
    }

    @Transactional
    public PedidoVenda cancelar(UUID tenantId, UUID usuarioId, UUID pedidoId) {
        return pedidoVendaService.cancelar(tenantId, usuarioId, pedidoId);
    }

    @Transactional
    public PedidoVenda alterarPagamento(UUID tenantId, UUID usuarioId, UUID pedidoId,
                                        UUID formaPagamentoId, UUID condicaoPagamentoId) {
        return pedidoVendaService.configurarPagamento(
                tenantId, usuarioId, pedidoId, formaPagamentoId, condicaoPagamentoId);
    }

    public record Comprovante(PedidoVendaDetalheConsultaService.Detalhe detalhe, Instant emitidoEm) {}
}
