package com.traxup.tplug.erp.pdv;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.venda.PedidoVendaItemApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PdvVendaItemSincronizacaoApplicationService {
    private final PdvVendaSincronizacaoRepository sincronizacaoRepository;
    private final PedidoVendaItemApplicationService itemService;

    public PdvVendaItemSincronizacaoApplicationService(PdvVendaSincronizacaoRepository sincronizacaoRepository,
                                                        PedidoVendaItemApplicationService itemService) {
        this.sincronizacaoRepository = sincronizacaoRepository;
        this.itemService = itemService;
    }

    @Transactional
    public PedidoVendaItemApplicationService.ResultadoPdv sincronizar(UUID tenantId, UUID usuarioId,
                                                                       UUID sincronizacaoId, UUID itemLocalId,
                                                                       UUID produtoId, UUID gradeId,
                                                                       BigDecimal quantidade, BigDecimal precoUnitario) {
        if (sincronizacaoId == null) throw new IllegalArgumentException("Sincronizacao PDV e obrigatoria");
        PdvVendaSincronizacao sincronizacao = sincronizacaoRepository.findById(sincronizacaoId)
                .filter(s -> s.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sincronizacao PDV nao encontrada para o tenant informado"));
        if (!sincronizacao.possuiPedidoVenda()) {
            throw new IllegalArgumentException("Sincronizacao PDV ainda nao possui pedido de venda vinculado");
        }
        return itemService.adicionarPdvIdempotente(tenantId, usuarioId, sincronizacao.getPedidoVendaId(),
                sincronizacao.getId(), itemLocalId, produtoId, gradeId, quantidade, precoUnitario);
    }
}
