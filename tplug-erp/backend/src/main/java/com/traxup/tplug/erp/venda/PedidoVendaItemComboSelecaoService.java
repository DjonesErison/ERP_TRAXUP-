package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.produto.combo.ProdutoComboGrupo;
import com.traxup.tplug.erp.produto.combo.ProdutoComboGrupoOpcao;
import com.traxup.tplug.erp.produto.combo.ProdutoComboGrupoOpcaoRepository;
import com.traxup.tplug.erp.produto.combo.ProdutoComboGrupoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PedidoVendaItemComboSelecaoService {
    private final PedidoVendaRepository pedidoRepository;
    private final PedidoVendaItemRepository itemRepository;
    private final ProdutoComboGrupoRepository grupoRepository;
    private final ProdutoComboGrupoOpcaoRepository opcaoRepository;
    private final PedidoVendaItemComboOpcaoRepository selecaoRepository;
    private final AuditoriaApplicationService auditoria;

    public PedidoVendaItemComboSelecaoService(PedidoVendaRepository pedidoRepository,
                                               PedidoVendaItemRepository itemRepository,
                                               ProdutoComboGrupoRepository grupoRepository,
                                               ProdutoComboGrupoOpcaoRepository opcaoRepository,
                                               PedidoVendaItemComboOpcaoRepository selecaoRepository,
                                               AuditoriaApplicationService auditoria) {
        this.pedidoRepository = pedidoRepository;
        this.itemRepository = itemRepository;
        this.grupoRepository = grupoRepository;
        this.opcaoRepository = opcaoRepository;
        this.selecaoRepository = selecaoRepository;
        this.auditoria = auditoria;
    }

    public List<PedidoVendaItemComboOpcao> listar(UUID tenantId, UUID pedidoId, UUID itemId) {
        buscarItem(tenantId, pedidoId, itemId);
        return selecaoRepository.findAllByTenantIdAndPedidoVendaItemIdOrderByGrupoIdAscOpcaoIdAsc(tenantId, itemId);
    }

    @Transactional
    public List<PedidoVendaItemComboOpcao> configurar(UUID tenantId, UUID usuarioId, UUID pedidoId, UUID itemId,
                                                       List<UUID> opcaoIds) {
        PedidoVenda pedido = buscarPedido(tenantId, pedidoId, true);
        PedidoVendaItem item = buscarItem(tenantId, pedidoId, itemId);
        List<ProdutoComboGrupo> grupos = grupoRepository
                .findAllByTenantIdAndComboProdutoIdOrderByNomeAsc(tenantId, item.getProdutoId());

        List<UUID> ids = opcaoIds == null ? List.of() : opcaoIds;
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException("A mesma opcao do combo nao pode ser selecionada mais de uma vez");
        }

        Map<UUID, ProdutoComboGrupo> grupoPorId = new HashMap<>();
        for (ProdutoComboGrupo grupo : grupos) grupoPorId.put(grupo.getId(), grupo);

        Map<UUID, Integer> quantidadePorGrupo = new HashMap<>();
        Map<UUID, ProdutoComboGrupoOpcao> opcaoPorId = new HashMap<>();
        BigDecimal adicionalUnitario = BigDecimal.ZERO;
        for (UUID opcaoId : ids) {
            ProdutoComboGrupoOpcao opcao = opcaoRepository.findByIdAndTenantId(opcaoId, tenantId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Opcao do combo nao encontrada para o tenant informado"));
            if (!grupoPorId.containsKey(opcao.getGrupoId())) {
                throw new IllegalArgumentException("Opcao informada nao pertence ao combo do item");
            }
            opcaoPorId.put(opcaoId, opcao);
            quantidadePorGrupo.merge(opcao.getGrupoId(), 1, Integer::sum);
            adicionalUnitario = adicionalUnitario.add(opcao.getValorAdicional());
        }

        for (ProdutoComboGrupo grupo : grupos) {
            int quantidade = quantidadePorGrupo.getOrDefault(grupo.getId(), 0);
            if (quantidade < grupo.getMinimoEscolhas() || quantidade > grupo.getMaximoEscolhas()) {
                throw new IllegalArgumentException("Quantidade de escolhas invalida para o grupo " + grupo.getNome());
            }
        }

        selecaoRepository.deleteByTenantIdAndPedidoVendaItemId(tenantId, itemId);
        selecaoRepository.flush();
        for (UUID opcaoId : ids) {
            ProdutoComboGrupoOpcao opcao = opcaoPorId.get(opcaoId);
            selecaoRepository.save(new PedidoVendaItemComboOpcao(
                    tenantId, itemId, opcao.getGrupoId(), opcaoId,
                    opcao.getProdutoId(), opcao.getQuantidade(), opcao.getValorAdicional()));
        }
        item.definirAdicionalComboUnitario(adicionalUnitario);
        itemRepository.save(item);

        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(),
                "ALTERAR", "PEDIDO_VENDA_ITEM_COMBO", itemId,
                "pedidoId=" + pedidoId + ";opcoes=" + ids.size() + ";adicionalUnitario=" + adicionalUnitario);
        return listar(tenantId, pedidoId, itemId);
    }

    private PedidoVenda buscarPedido(UUID tenantId, UUID pedidoId, boolean exigirRascunho) {
        PedidoVenda pedido = pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de venda nao encontrado para o tenant informado"));
        if (exigirRascunho && !"RASCUNHO".equals(pedido.getStatus())) {
            throw new IllegalArgumentException("Opcoes do combo so podem ser alteradas enquanto o pedido estiver em RASCUNHO");
        }
        return pedido;
    }

    private PedidoVendaItem buscarItem(UUID tenantId, UUID pedidoId, UUID itemId) {
        return itemRepository.findByIdAndTenantIdAndPedidoVendaId(itemId, tenantId, pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do pedido de venda nao encontrado para o tenant informado"));
    }
}
