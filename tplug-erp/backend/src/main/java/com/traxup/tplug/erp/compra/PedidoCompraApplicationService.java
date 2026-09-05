package com.traxup.tplug.erp.compra;

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
public class PedidoCompraApplicationService {

    private final PedidoCompraRepository repository;
    private final FilialRepository filialRepository;
    private final PessoaRepository pessoaRepository;

    public PedidoCompraApplicationService(PedidoCompraRepository repository,
                                          FilialRepository filialRepository,
                                          PessoaRepository pessoaRepository) {
        this.repository = repository;
        this.filialRepository = filialRepository;
        this.pessoaRepository = pessoaRepository;
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
            throw new IllegalArgumentException("Pessoa informada nao e um fornecedor ativo");
        }

        String numeroNormalizado = numero.trim();
        if (repository.existsByTenantIdAndNumeroIgnoreCase(tenantId, numeroNormalizado)) {
            throw new IllegalArgumentException("Numero de pedido de compra ja cadastrado para o tenant");
        }

        String observacaoNormalizada = observacao == null || observacao.isBlank() ? null : observacao.trim();
        return repository.save(new PedidoCompra(tenantId, filialId, fornecedorId, numeroNormalizado, observacaoNormalizada));
    }
}
