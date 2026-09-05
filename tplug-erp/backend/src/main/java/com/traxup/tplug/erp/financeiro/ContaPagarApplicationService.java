package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.Pessoa;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ContaPagarApplicationService {
    private final ContaPagarRepository repository;
    private final FilialRepository filialRepository;
    private final PessoaRepository pessoaRepository;
    private final AuditoriaApplicationService auditoria;

    public ContaPagarApplicationService(ContaPagarRepository repository,
                                        FilialRepository filialRepository,
                                        PessoaRepository pessoaRepository,
                                        AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.filialRepository = filialRepository;
        this.pessoaRepository = pessoaRepository;
        this.auditoria = auditoria;
    }

    public List<ContaPagar> listar(UUID tenantId) {
        return repository.findAllByTenantIdOrderByVencimentoAscCriadoEmDesc(tenantId);
    }

    public ContaPagar buscar(UUID tenantId, UUID contaId) {
        return repository.findByIdAndTenantId(contaId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta a pagar nao encontrada para o tenant informado"));
    }

    @Transactional
    public ContaPagar criar(UUID tenantId, UUID usuarioId, UUID filialId, UUID fornecedorId,
                            String numeroDocumento, String descricao, BigDecimal valorOriginal,
                            LocalDate vencimento) {
        if (!filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }

        Pessoa fornecedor = pessoaRepository.findByIdAndTenantId(fornecedorId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor nao encontrado para o tenant informado"));
        if (!fornecedor.isFornecedor() || !fornecedor.isAtivo()) {
            throw new IllegalArgumentException("Pessoa informada nao e um fornecedor ativo");
        }
        if (valorOriginal == null || valorOriginal.signum() <= 0) {
            throw new IllegalArgumentException("Valor original deve ser maior que zero");
        }
        if (vencimento == null) throw new IllegalArgumentException("Vencimento e obrigatorio");

        ContaPagar conta = repository.save(new ContaPagar(
                tenantId, filialId, fornecedorId,
                normalizarObrigatorio(numeroDocumento, "Numero do documento"),
                normalizarObrigatorio(descricao, "Descricao"),
                valorOriginal, vencimento, usuarioId));

        auditoria.registrar(tenantId, usuarioId, null, filialId,
                "CRIAR", "CONTA_PAGAR", conta.getId(),
                "fornecedorId=" + fornecedorId + ";valor=" + valorOriginal);
        return conta;
    }

    @Transactional
    public ContaPagar pagar(UUID tenantId, UUID usuarioId, UUID contaId) {
        ContaPagar conta = buscar(tenantId, contaId);
        conta.pagar();
        repository.save(conta);
        auditoria.registrar(tenantId, usuarioId, null, conta.getFilialId(),
                "BAIXAR", "CONTA_PAGAR", conta.getId(), "valor=" + conta.getValorPago());
        return conta;
    }

    @Transactional
    public ContaPagar cancelar(UUID tenantId, UUID usuarioId, UUID contaId) {
        ContaPagar conta = buscar(tenantId, contaId);
        conta.cancelar();
        repository.save(conta);
        auditoria.registrar(tenantId, usuarioId, null, conta.getFilialId(),
                "CANCELAR", "CONTA_PAGAR", conta.getId(), null);
        return conta;
    }

    private String normalizarObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " e obrigatorio");
        return valor.trim();
    }
}
