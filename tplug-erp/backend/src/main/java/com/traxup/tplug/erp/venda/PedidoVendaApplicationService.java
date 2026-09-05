package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.estoque.EstoqueMovimentacaoApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.Pessoa;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PedidoVendaApplicationService {
    private final PedidoVendaRepository repository;
    private final PedidoVendaItemRepository itemRepository;
    private final FilialRepository filialRepository;
    private final PessoaRepository pessoaRepository;
    private final EstoqueMovimentacaoApplicationService estoqueMovimentacaoService;
    private final AuditoriaApplicationService auditoria;

    public PedidoVendaApplicationService(PedidoVendaRepository repository, PedidoVendaItemRepository itemRepository,
                                         FilialRepository filialRepository, PessoaRepository pessoaRepository,
                                         EstoqueMovimentacaoApplicationService estoqueMovimentacaoService,
                                         AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.filialRepository = filialRepository;
        this.pessoaRepository = pessoaRepository;
        this.estoqueMovimentacaoService = estoqueMovimentacaoService;
        this.auditoria = auditoria;
    }

    public List<PedidoVenda> listar(UUID tenantId) { return repository.findAllByTenantIdOrderByCriadoEmDesc(tenantId); }

    public PedidoVenda buscar(UUID tenantId, UUID pedidoId) {
        return repository.findByIdAndTenantId(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de venda nao encontrado para o tenant informado"));
    }

    public List<PedidoVendaItem> listarItens(UUID tenantId, UUID pedidoId) {
        buscar(tenantId, pedidoId);
        return itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAsc(tenantId, pedidoId);
    }

    @Transactional
    public PedidoVenda criar(UUID tenantId, UUID filialId, UUID clienteId, String numero, String observacao, UUID usuarioId) {
        if (!filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }
        if (clienteId != null) {
            Pessoa cliente = pessoaRepository.findByIdAndTenantId(clienteId, tenantId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado para o tenant informado"));
            if (!cliente.isCliente() || !cliente.isAtivo()) throw new IllegalArgumentException("Pessoa informada nao e um cliente ativo");
        }
        String numeroNormalizado = numero.trim();
        if (repository.existsByTenantIdAndNumeroIgnoreCase(tenantId, numeroNormalizado)) {
            throw new IllegalArgumentException("Numero de pedido de venda ja cadastrado para o tenant");
        }
        String observacaoNormalizada = observacao == null || observacao.isBlank() ? null : observacao.trim();
        PedidoVenda pedido = repository.save(new PedidoVenda(tenantId, filialId, clienteId, numeroNormalizado, observacaoNormalizada, usuarioId));
        auditoria.registrar(tenantId, usuarioId, null, filialId, "CRIAR", "PEDIDO_VENDA", pedido.getId(), null);
        return pedido;
    }

    @Transactional
    public PedidoVenda abrir(UUID tenantId, UUID usuarioId, UUID pedidoId) {
        PedidoVenda pedido = buscar(tenantId, pedidoId);
        if (itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAsc(tenantId, pedidoId).isEmpty()) {
            throw new IllegalArgumentException("Pedido de venda precisa possuir itens antes de ser aberto");
        }
        pedido.abrir();
        repository.save(pedido);
        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(), "ABRIR", "PEDIDO_VENDA", pedido.getId(), null);
        return pedido;
    }

    @Transactional
    public PedidoVenda faturar(UUID tenantId, UUID usuarioId, UUID pedidoId) {
        PedidoVenda pedido = buscar(tenantId, pedidoId);
        if (!"ABERTO".equals(pedido.getStatus())) {
            throw new IllegalArgumentException("Somente pedido de venda ABERTO pode ser faturado");
        }
        List<PedidoVendaItem> itens = itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAsc(tenantId, pedidoId);
        if (itens.isEmpty()) throw new IllegalArgumentException("Pedido de venda precisa possuir itens antes de ser faturado");
        for (PedidoVendaItem item : itens) {
            String tipoItem = item.getGradeId() == null ? "PRODUTO" : "GRADE";
            UUID itemEstoqueId = item.getGradeId() == null ? item.getProdutoId() : item.getGradeId();
            estoqueMovimentacaoService.movimentar(tenantId, pedido.getFilialId(), tipoItem, itemEstoqueId,
                    "SAIDA", item.getQuantidade(), "FATURAMENTO_PEDIDO_VENDA:" + pedido.getId(), usuarioId);
        }
        pedido.faturar();
        repository.save(pedido);
        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(),
                "FATURAR", "PEDIDO_VENDA", pedido.getId(), "itens=" + itens.size());
        return pedido;
    }

    @Transactional
    public PedidoVenda cancelar(UUID tenantId, UUID usuarioId, UUID pedidoId) {
        PedidoVenda pedido = buscar(tenantId, pedidoId);
        pedido.cancelar();
        repository.save(pedido);
        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(), "CANCELAR", "PEDIDO_VENDA", pedido.getId(), null);
        return pedido;
    }
}
