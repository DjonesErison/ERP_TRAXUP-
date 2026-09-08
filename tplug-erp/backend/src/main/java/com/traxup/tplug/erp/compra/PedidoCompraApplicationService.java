package com.traxup.tplug.erp.compra;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.Pessoa;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PedidoCompraApplicationService {
    private final PedidoCompraRepository repository;
    private final PedidoCompraItemRepository itemRepository;
    private final FilialRepository filialRepository;
    private final PessoaRepository pessoaRepository;
    private final AuditoriaApplicationService auditoria;

    public PedidoCompraApplicationService(PedidoCompraRepository repository, PedidoCompraItemRepository itemRepository,
                                          FilialRepository filialRepository, PessoaRepository pessoaRepository,
                                          AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.filialRepository = filialRepository;
        this.pessoaRepository = pessoaRepository;
        this.auditoria = auditoria;
    }

    public List<PedidoCompra> listar(UUID tenantId) {
        return repository.findAllByTenantIdOrderByCriadoEmDesc(tenantId);
    }

    public PedidoCompra buscar(UUID tenantId, UUID pedidoId) {
        return repository.findByIdAndTenantId(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de compra nao encontrado para o tenant informado"));
    }

    @Transactional
    public PedidoCompra criar(UUID tenantId, UUID filialId, UUID fornecedorId, String numero, String observacao) {
        if (!filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }
        Pessoa fornecedor = pessoaRepository.findByIdAndTenantId(fornecedorId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor nao encontrado para o tenant informado"));
        if (!fornecedor.isFornecedor() || !fornecedor.isAtivo()) {
            throw new RegraNegocioException("Pessoa informada nao e um fornecedor ativo");
        }
        if (numero == null || numero.isBlank()) {
            throw new RegraNegocioException("Numero do pedido de compra e obrigatorio");
        }
        String numeroNormalizado = numero.trim();
        if (repository.existsByTenantIdAndNumeroIgnoreCase(tenantId, numeroNormalizado)) {
            throw new RecursoConflitanteException("Numero de pedido de compra ja cadastrado para o tenant");
        }
        String observacaoNormalizada = observacao == null || observacao.isBlank() ? null : observacao.trim();
        return repository.save(new PedidoCompra(tenantId, filialId, fornecedorId, numeroNormalizado, observacaoNormalizada));
    }

    @Transactional
    public PedidoCompra abrir(UUID tenantId, UUID usuarioId, UUID pedidoId) {
        PedidoCompra pedido = buscarParaAtualizacao(tenantId, pedidoId);
        if (!"RASCUNHO".equals(pedido.getStatus())) {
            throw new RegraNegocioException("Somente pedido em RASCUNHO pode ser aberto");
        }
        if (itemRepository.findAllByTenantIdAndPedidoCompraIdOrderByCriadoEmAsc(tenantId, pedidoId).isEmpty()) {
            throw new RegraNegocioException("Pedido de compra precisa possuir itens antes de ser aberto");
        }
        pedido.abrir();
        repository.save(pedido);
        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(), "ABRIR", "PEDIDO_COMPRA", pedido.getId(), null);
        return pedido;
    }

    @Transactional
    public PedidoCompra cancelar(UUID tenantId, UUID usuarioId, UUID pedidoId) {
        PedidoCompra pedido = buscarParaAtualizacao(tenantId, pedidoId);
        if ("CANCELADO".equals(pedido.getStatus())) {
            throw new RecursoConflitanteException("Pedido de compra ja esta cancelado");
        }
        if ("RECEBIDO".equals(pedido.getStatus())) {
            throw new RegraNegocioException("Pedido recebido nao pode ser cancelado");
        }
        pedido.cancelar();
        repository.save(pedido);
        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(), "CANCELAR", "PEDIDO_COMPRA", pedido.getId(), null);
        return pedido;
    }

    @Transactional
    public PedidoCompra marcarRecebido(UUID tenantId, UUID usuarioId, UUID pedidoId) {
        PedidoCompra pedido = buscarParaAtualizacao(tenantId, pedidoId);
        if (!"ABERTO".equals(pedido.getStatus())) {
            throw new RegraNegocioException("Somente pedido ABERTO pode ser marcado como recebido");
        }
        pedido.marcarRecebido();
        repository.save(pedido);
        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(), "RECEBER", "PEDIDO_COMPRA", pedido.getId(), null);
        return pedido;
    }

    private PedidoCompra buscarParaAtualizacao(UUID tenantId, UUID pedidoId) {
        return repository.findByIdAndTenantIdForUpdate(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de compra nao encontrado para o tenant informado"));
    }
}
