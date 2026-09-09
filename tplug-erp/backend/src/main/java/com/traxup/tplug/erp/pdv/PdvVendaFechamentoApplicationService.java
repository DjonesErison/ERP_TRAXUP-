package com.traxup.tplug.erp.pdv;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.venda.PedidoVenda;
import com.traxup.tplug.erp.venda.PedidoVendaApplicationService;
import com.traxup.tplug.erp.venda.PedidoVendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PdvVendaFechamentoApplicationService {
    private final PdvVendaSincronizacaoRepository sincronizacaoRepository;
    private final PedidoVendaRepository pedidoVendaRepository;
    private final PedidoVendaApplicationService pedidoVendaService;

    public PdvVendaFechamentoApplicationService(PdvVendaSincronizacaoRepository sincronizacaoRepository,
                                                 PedidoVendaRepository pedidoVendaRepository,
                                                 PedidoVendaApplicationService pedidoVendaService) {
        this.sincronizacaoRepository = sincronizacaoRepository;
        this.pedidoVendaRepository = pedidoVendaRepository;
        this.pedidoVendaService = pedidoVendaService;
    }

    @Transactional
    public Resultado fechar(UUID tenantId, UUID usuarioId, UUID sincronizacaoId,
                            UUID formaPagamentoId, UUID condicaoPagamentoId) {
        if (sincronizacaoId == null) throw new IllegalArgumentException("Sincronizacao PDV e obrigatoria");
        if (formaPagamentoId == null) throw new IllegalArgumentException("Forma de pagamento e obrigatoria");
        if (condicaoPagamentoId == null) throw new IllegalArgumentException("Condicao de pagamento e obrigatoria");

        PdvVendaSincronizacao sincronizacao = sincronizacaoRepository.findById(sincronizacaoId)
                .filter(s -> s.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sincronizacao PDV nao encontrada para o tenant informado"));
        if (!sincronizacao.possuiPedidoVenda()) {
            throw new IllegalArgumentException("Sincronizacao PDV ainda nao possui pedido de venda vinculado");
        }

        PedidoVenda pedido = pedidoVendaRepository.buscarParaAtualizar(sincronizacao.getPedidoVendaId(), tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de venda nao encontrado para o tenant informado"));

        if ("FATURADO".equals(pedido.getStatus())) {
            validarMesmoPagamento(pedido, formaPagamentoId, condicaoPagamentoId);
            return new Resultado(pedido, true);
        }
        if ("CANCELADO".equals(pedido.getStatus())) {
            throw new IllegalArgumentException("Venda PDV cancelada nao pode ser faturada");
        }

        if ("RASCUNHO".equals(pedido.getStatus())) {
            pedidoVendaService.configurarPagamento(tenantId, usuarioId, pedido.getId(), formaPagamentoId, condicaoPagamentoId);
            pedidoVendaService.abrir(tenantId, usuarioId, pedido.getId());
        } else if ("ABERTO".equals(pedido.getStatus())) {
            validarMesmoPagamento(pedido, formaPagamentoId, condicaoPagamentoId);
        } else {
            throw new IllegalArgumentException("Pedido de venda em estado invalido para fechamento PDV: " + pedido.getStatus());
        }

        PedidoVenda faturado = pedidoVendaService.faturar(tenantId, usuarioId, pedido.getId());
        return new Resultado(faturado, false);
    }

    private void validarMesmoPagamento(PedidoVenda pedido, UUID formaPagamentoId, UUID condicaoPagamentoId) {
        if (!Objects.equals(pedido.getFormaPagamentoId(), formaPagamentoId)
                || !Objects.equals(pedido.getCondicaoPagamentoId(), condicaoPagamentoId)) {
            throw new IllegalArgumentException("Venda PDV ja processada com configuracao de pagamento diferente");
        }
    }

    public record Resultado(PedidoVenda pedidoVenda, boolean repetida) {}
}
