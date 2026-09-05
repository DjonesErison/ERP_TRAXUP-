package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
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
    private final FilialRepository filialRepository;
    private final PessoaRepository pessoaRepository;
    private final AuditoriaApplicationService auditoria;

    public PedidoVendaApplicationService(PedidoVendaRepository repository, FilialRepository filialRepository,
                                         PessoaRepository pessoaRepository, AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.filialRepository = filialRepository;
        this.pessoaRepository = pessoaRepository;
        this.auditoria = auditoria;
    }

    public List<PedidoVenda> listar(UUID tenantId) { return repository.findAllByTenantIdOrderByCriadoEmDesc(tenantId); }

    public PedidoVenda buscar(UUID tenantId, UUID pedidoId) {
        return repository.findByIdAndTenantId(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de venda nao encontrado para o tenant informado"));
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
    public PedidoVenda cancelar(UUID tenantId, UUID usuarioId, UUID pedidoId) {
        PedidoVenda pedido = buscar(tenantId, pedidoId);
        pedido.cancelar();
        repository.save(pedido);
        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(), "CANCELAR", "PEDIDO_VENDA", pedido.getId(), null);
        return pedido;
    }
}
