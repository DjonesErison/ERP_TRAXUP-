package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.Pessoa;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ContaReceberApplicationService {
    private final ContaReceberRepository repository;
    private final ContaReceberMovimentoRepository movimentoRepository;
    private final FilialRepository filialRepository;
    private final PessoaRepository pessoaRepository;
    private final AuditoriaApplicationService auditoria;

    public ContaReceberApplicationService(ContaReceberRepository repository,
                                          ContaReceberMovimentoRepository movimentoRepository,
                                          FilialRepository filialRepository,
                                          PessoaRepository pessoaRepository,
                                          AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.movimentoRepository = movimentoRepository;
        this.filialRepository = filialRepository;
        this.pessoaRepository = pessoaRepository;
        this.auditoria = auditoria;
    }

    public List<ContaReceber> listar(UUID tenantId) {
        return repository.findAllByTenantIdOrderByVencimentoAscCriadoEmDesc(tenantId);
    }

    public ContaReceber buscar(UUID tenantId, UUID contaId) {
        return repository.findByIdAndTenantId(contaId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Conta a receber nao encontrada para o tenant informado"));
    }

    public List<ContaReceberMovimento> listarMovimentos(UUID tenantId, UUID contaId) {
        buscar(tenantId, contaId);
        return movimentoRepository
                .findAllByTenantIdAndContaReceberIdOrderByDataMovimentoDescCriadoEmDesc(tenantId, contaId);
    }

    @Transactional
    public ContaReceber criar(UUID tenantId, UUID usuarioId, UUID filialId, UUID clienteId,
                              String numeroDocumento, String descricao, BigDecimal valorOriginal,
                              LocalDate vencimento) {
        if (!filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }

        Pessoa cliente = pessoaRepository.findByIdAndTenantId(clienteId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente nao encontrado para o tenant informado"));
        if (!cliente.isCliente() || !cliente.isAtivo()) {
            throw new RegraNegocioException("Pessoa informada nao e um cliente ativo");
        }

        if (valorOriginal == null || valorOriginal.signum() <= 0) {
            throw new RegraNegocioException("Valor original deve ser maior que zero");
        }
        if (vencimento == null) throw new RegraNegocioException("Vencimento e obrigatorio");

        ContaReceber conta = repository.save(new ContaReceber(
                tenantId,
                filialId,
                clienteId,
                normalizarObrigatorio(numeroDocumento, "Numero do documento"),
                normalizarObrigatorio(descricao, "Descricao"),
                valorOriginal,
                vencimento,
                usuarioId));

        auditoria.registrar(tenantId, usuarioId, null, filialId,
                "CRIAR", "CONTA_RECEBER", conta.getId(),
                "clienteId=" + clienteId + ";valor=" + valorOriginal);
        return conta;
    }

    @Transactional
    public ContaReceber receber(UUID tenantId, UUID usuarioId, UUID contaId) {
        ContaReceber conta = buscarParaBaixa(tenantId, contaId);
        registrarMovimento(tenantId, usuarioId, conta, conta.saldoAberto(), LocalDate.now(), null);
        return conta;
    }

    @Transactional
    public ContaReceberMovimento registrarRecebimento(UUID tenantId, UUID usuarioId, UUID contaId,
                                                       BigDecimal valor, LocalDate dataMovimento,
                                                       String observacao) {
        ContaReceber conta = buscarParaBaixa(tenantId, contaId);
        return registrarMovimento(tenantId, usuarioId, conta, valor, dataMovimento, observacao);
    }

    @Transactional
    public ContaReceber cancelar(UUID tenantId, UUID usuarioId, UUID contaId) {
        ContaReceber conta = buscarParaBaixa(tenantId, contaId);
        conta.cancelar();
        repository.save(conta);
        auditoria.registrar(tenantId, usuarioId, null, conta.getFilialId(),
                "CANCELAR", "CONTA_RECEBER", conta.getId(), null);
        return conta;
    }

    private ContaReceberMovimento registrarMovimento(UUID tenantId, UUID usuarioId, ContaReceber conta,
                                                      BigDecimal valor, LocalDate dataMovimento,
                                                      String observacao) {
        if (dataMovimento == null) {
            throw new RegraNegocioException("Data do recebimento e obrigatoria");
        }
        if (dataMovimento.isAfter(LocalDate.now())) {
            throw new RegraNegocioException("Data do recebimento nao pode estar no futuro");
        }

        conta.registrarRecebimento(valor);
        repository.save(conta);

        ContaReceberMovimento movimento = movimentoRepository.save(new ContaReceberMovimento(
                tenantId, conta.getId(), valor, dataMovimento, normalizar(observacao), usuarioId));

        auditoria.registrar(tenantId, usuarioId, null, conta.getFilialId(),
                "BAIXAR", "CONTA_RECEBER", conta.getId(),
                "movimentoId=" + movimento.getId() + ";valor=" + valor + ";saldo=" + conta.saldoAberto());
        return movimento;
    }

    private ContaReceber buscarParaBaixa(UUID tenantId, UUID contaId) {
        return repository.findByIdAndTenantIdForUpdate(contaId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Conta a receber nao encontrada para o tenant informado"));
    }

    private String normalizarObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new RegraNegocioException(campo + " e obrigatorio");
        return valor.trim();
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
