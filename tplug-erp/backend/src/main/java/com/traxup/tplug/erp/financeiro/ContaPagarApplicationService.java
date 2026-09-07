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
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ContaPagarApplicationService {
    private final ContaPagarRepository repository;
    private final ContaPagarPagamentoRepository pagamentoRepository;
    private final FilialRepository filialRepository;
    private final PessoaRepository pessoaRepository;
    private final AuditoriaApplicationService auditoria;

    public ContaPagarApplicationService(ContaPagarRepository repository, ContaPagarPagamentoRepository pagamentoRepository, FilialRepository filialRepository, PessoaRepository pessoaRepository, AuditoriaApplicationService auditoria) {
        this.repository = repository; this.pagamentoRepository = pagamentoRepository; this.filialRepository = filialRepository; this.pessoaRepository = pessoaRepository; this.auditoria = auditoria;
    }

    public List<ContaPagar> listar(UUID tenantId) { return listar(tenantId, null, null, null); }
    public List<ContaPagar> listar(UUID tenantId, String status, LocalDate vencimentoInicio, LocalDate vencimentoFim) { validarPeriodo(vencimentoInicio, vencimentoFim); return repository.filtrar(tenantId, normalizarStatus(status), vencimentoInicio, vencimentoFim); }
    public List<ContaPagar> listar(UUID tenantId, UUID filialId, String status, LocalDate vencimentoInicio, LocalDate vencimentoFim) { if (filialId == null) return listar(tenantId, status, vencimentoInicio, vencimentoFim); validarPeriodo(vencimentoInicio, vencimentoFim); return repository.filtrarPorFilial(tenantId, filialId, normalizarStatus(status), vencimentoInicio, vencimentoFim); }
    public List<ContaPagar> listar(UUID tenantId, UUID filialId, UUID fornecedorId, String status, LocalDate vencimentoInicio, LocalDate vencimentoFim) { if (fornecedorId == null) return listar(tenantId, filialId, status, vencimentoInicio, vencimentoFim); validarPeriodo(vencimentoInicio, vencimentoFim); return repository.filtrarPorFornecedor(tenantId, fornecedorId, filialId, normalizarStatus(status), vencimentoInicio, vencimentoFim); }

    public TituloFinanceiroResumo resumir(UUID tenantId, String status, LocalDate vencimentoInicio, LocalDate vencimentoFim) { return resumir(tenantId, null, null, status, vencimentoInicio, vencimentoFim); }
    public TituloFinanceiroResumo resumir(UUID tenantId, UUID filialId, String status, LocalDate vencimentoInicio, LocalDate vencimentoFim) { return resumir(tenantId, filialId, null, status, vencimentoInicio, vencimentoFim); }
    public TituloFinanceiroResumo resumir(UUID tenantId, UUID filialId, UUID fornecedorId, String status, LocalDate vencimentoInicio, LocalDate vencimentoFim) {
        validarPeriodo(vencimentoInicio, vencimentoFim);
        return TituloFinanceiroResumo.deProjection(repository.resumir(
                tenantId, filialId, fornecedorId, normalizarStatus(status), vencimentoInicio, vencimentoFim));
    }

    public ContaPagar buscar(UUID tenantId, UUID contaId) { return repository.findByIdAndTenantId(contaId, tenantId).orElseThrow(() -> new RecursoNaoEncontradoException("Conta a pagar nao encontrada para o tenant informado")); }
    public List<ContaPagarPagamento> listarPagamentos(UUID tenantId, UUID contaId) { buscar(tenantId, contaId); return pagamentoRepository.findAllByTenantIdAndContaPagarIdOrderByPagoEmDesc(tenantId, contaId); }

    @Transactional
    public ContaPagar criar(UUID tenantId, UUID usuarioId, UUID filialId, UUID fornecedorId, String numeroDocumento, String descricao, BigDecimal valorOriginal, LocalDate vencimento) {
        if (!filialRepository.existsByIdAndTenantId(filialId, tenantId)) throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        Pessoa fornecedor = pessoaRepository.findByIdAndTenantId(fornecedorId, tenantId).orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor nao encontrado para o tenant informado"));
        if (!fornecedor.isFornecedor() || !fornecedor.isAtivo()) throw new RegraNegocioException("Pessoa informada nao e um fornecedor ativo");
        if (valorOriginal == null || valorOriginal.signum() <= 0) throw new RegraNegocioException("Valor original deve ser maior que zero");
        if (vencimento == null) throw new RegraNegocioException("Vencimento e obrigatorio");
        ContaPagar conta = repository.save(new ContaPagar(tenantId, filialId, fornecedorId, normalizarObrigatorio(numeroDocumento, "Numero do documento"), normalizarObrigatorio(descricao, "Descricao"), valorOriginal, vencimento, usuarioId));
        auditoria.registrar(tenantId, usuarioId, null, filialId, "CRIAR", "CONTA_PAGAR", conta.getId(), "fornecedorId=" + fornecedorId + ";valor=" + valorOriginal);
        return conta;
    }

    @Transactional public ContaPagar pagar(UUID tenantId, UUID usuarioId, UUID contaId) { ContaPagar conta = buscarParaAtualizacao(tenantId, contaId); return registrarPagamento(tenantId, usuarioId, conta, conta.getSaldoAberto()); }
    @Transactional public ContaPagar pagar(UUID tenantId, UUID usuarioId, UUID contaId, BigDecimal valor) { return registrarPagamento(tenantId, usuarioId, buscarParaAtualizacao(tenantId, contaId), valor); }
    @Transactional public ContaPagar cancelar(UUID tenantId, UUID usuarioId, UUID contaId) { ContaPagar conta = buscarParaAtualizacao(tenantId, contaId); conta.cancelar(); repository.save(conta); auditoria.registrar(tenantId, usuarioId, null, conta.getFilialId(), "CANCELAR", "CONTA_PAGAR", conta.getId(), null); return conta; }

    ContaPagar buscarParaAtualizacao(UUID tenantId, UUID contaId) { return repository.findByIdAndTenantIdForUpdate(contaId, tenantId).orElseThrow(() -> new RecursoNaoEncontradoException("Conta a pagar nao encontrada para o tenant informado")); }
    private ContaPagar registrarPagamento(UUID tenantId, UUID usuarioId, ContaPagar conta, BigDecimal valor) { conta.pagar(valor); ContaPagarPagamento pagamento = pagamentoRepository.save(new ContaPagarPagamento(tenantId, conta.getFilialId(), conta.getId(), valor, usuarioId)); repository.save(conta); auditoria.registrar(tenantId, usuarioId, null, conta.getFilialId(), "BAIXAR", "CONTA_PAGAR", conta.getId(), "pagamentoId=" + pagamento.getId() + ";valor=" + valor + ";status=" + conta.getStatus()); return conta; }

    private String normalizarStatus(String status) { if (status == null || status.isBlank()) return null; String n = status.trim().toUpperCase(Locale.ROOT); if (!"ABERTO".equals(n) && !"PARCIAL".equals(n) && !"PAGO".equals(n) && !"CANCELADO".equals(n)) throw new RegraNegocioException("Status de conta a pagar invalido"); return n; }
    private void validarPeriodo(LocalDate inicio, LocalDate fim) { if (inicio != null && fim != null && inicio.isAfter(fim)) throw new RegraNegocioException("Vencimento inicial nao pode ser posterior ao vencimento final"); }
    private String normalizarObrigatorio(String valor, String campo) { if (valor == null || valor.isBlank()) throw new RegraNegocioException(campo + " e obrigatorio"); return valor.trim(); }
}
