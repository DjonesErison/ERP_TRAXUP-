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
public class ContaReceberApplicationService {
    private final ContaReceberRepository repository;
    private final ContaReceberRecebimentoRepository recebimentoRepository;
    private final FilialRepository filialRepository;
    private final PessoaRepository pessoaRepository;
    private final AuditoriaApplicationService auditoria;

    public ContaReceberApplicationService(ContaReceberRepository repository, ContaReceberRecebimentoRepository recebimentoRepository, FilialRepository filialRepository, PessoaRepository pessoaRepository, AuditoriaApplicationService auditoria) {
        this.repository = repository; this.recebimentoRepository = recebimentoRepository; this.filialRepository = filialRepository; this.pessoaRepository = pessoaRepository; this.auditoria = auditoria;
    }

    public List<ContaReceber> listar(UUID tenantId) { return listar(tenantId, null, null, null); }

    public List<ContaReceber> listar(UUID tenantId, String status, LocalDate vencimentoInicio, LocalDate vencimentoFim) {
        validarPeriodo(vencimentoInicio, vencimentoFim);
        return repository.filtrar(tenantId, normalizarStatus(status), vencimentoInicio, vencimentoFim);
    }

    public List<ContaReceber> listar(UUID tenantId, UUID filialId, String status, LocalDate vencimentoInicio, LocalDate vencimentoFim) {
        if (filialId == null) return listar(tenantId, status, vencimentoInicio, vencimentoFim);
        validarPeriodo(vencimentoInicio, vencimentoFim);
        return repository.filtrarPorFilial(tenantId, filialId, normalizarStatus(status), vencimentoInicio, vencimentoFim);
    }

    public List<ContaReceber> listar(UUID tenantId, UUID filialId, UUID clienteId, String status, LocalDate vencimentoInicio, LocalDate vencimentoFim) {
        if (clienteId == null) return listar(tenantId, filialId, status, vencimentoInicio, vencimentoFim);
        validarPeriodo(vencimentoInicio, vencimentoFim);
        return repository.filtrarPorCliente(tenantId, clienteId, filialId, normalizarStatus(status), vencimentoInicio, vencimentoFim);
    }

    public TituloFinanceiroResumo resumir(UUID tenantId, String status, LocalDate vencimentoInicio, LocalDate vencimentoFim) {
        return resumir(tenantId, null, null, status, vencimentoInicio, vencimentoFim);
    }

    public TituloFinanceiroResumo resumir(UUID tenantId, UUID filialId, String status, LocalDate vencimentoInicio, LocalDate vencimentoFim) {
        return resumir(tenantId, filialId, null, status, vencimentoInicio, vencimentoFim);
    }

    public TituloFinanceiroResumo resumir(UUID tenantId, UUID filialId, UUID clienteId, String status, LocalDate vencimentoInicio, LocalDate vencimentoFim) {
        validarPeriodo(vencimentoInicio, vencimentoFim);
        return TituloFinanceiroResumo.deProjection(repository.resumir(
                tenantId, filialId, clienteId, normalizarStatus(status), vencimentoInicio, vencimentoFim));
    }

    public List<ContaReceber> listarPorOrigem(UUID tenantId, String origemTipo, UUID origemId) {
        if (origemId == null) throw new RegraNegocioException("Identificador da origem e obrigatorio");
        String tipo = normalizarObrigatorio(origemTipo, "Tipo da origem").toUpperCase(Locale.ROOT);
        return repository.findAllByTenantIdAndOrigemTipoAndOrigemIdOrderByVencimentoAscCriadoEmDescIdAsc(tenantId, tipo, origemId);
    }

    public ContaReceber buscar(UUID tenantId, UUID contaId) {
        return repository.findByIdAndTenantId(contaId, tenantId).orElseThrow(() -> new RecursoNaoEncontradoException("Conta a receber nao encontrada para o tenant informado"));
    }

    public List<ContaReceberRecebimento> listarRecebimentos(UUID tenantId, UUID contaId) {
        buscar(tenantId, contaId);
        return recebimentoRepository.findAllByTenantIdAndContaReceberIdOrderByRecebidoEmDesc(tenantId, contaId);
    }

    @Transactional
    public ContaReceber criar(UUID tenantId, UUID usuarioId, UUID filialId, UUID clienteId, String numeroDocumento, String descricao, BigDecimal valorOriginal, LocalDate vencimento) {
        return criarInterno(tenantId, usuarioId, filialId, clienteId, numeroDocumento, descricao, valorOriginal, vencimento, null, null, null);
    }

    @Transactional
    public ContaReceber criarComOrigem(UUID tenantId, UUID usuarioId, UUID filialId, UUID clienteId, String numeroDocumento, String descricao, BigDecimal valorOriginal, LocalDate vencimento, String origemTipo, UUID origemId, String origemReferencia) {
        String tipo = normalizarObrigatorio(origemTipo, "Tipo da origem").toUpperCase(Locale.ROOT);
        String referencia = normalizarObrigatorio(origemReferencia, "Referencia da origem");
        if (origemId == null) throw new RegraNegocioException("Identificador da origem e obrigatorio");
        return criarInterno(tenantId, usuarioId, filialId, clienteId, numeroDocumento, descricao, valorOriginal, vencimento, tipo, origemId, referencia);
    }

    private ContaReceber criarInterno(UUID tenantId, UUID usuarioId, UUID filialId, UUID clienteId, String numeroDocumento, String descricao, BigDecimal valorOriginal, LocalDate vencimento, String origemTipo, UUID origemId, String origemReferencia) {
        if (!filialRepository.existsByIdAndTenantId(filialId, tenantId)) throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        Pessoa cliente = pessoaRepository.findByIdAndTenantId(clienteId, tenantId).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado para o tenant informado"));
        if (!cliente.isCliente() || !cliente.isAtivo()) throw new RegraNegocioException("Pessoa informada nao e um cliente ativo");
        if (valorOriginal == null || valorOriginal.signum() <= 0) throw new RegraNegocioException("Valor original deve ser maior que zero");
        if (vencimento == null) throw new RegraNegocioException("Vencimento e obrigatorio");
        ContaReceber conta = repository.save(new ContaReceber(tenantId, filialId, clienteId, normalizarObrigatorio(numeroDocumento, "Numero do documento"), normalizarObrigatorio(descricao, "Descricao"), valorOriginal, vencimento, usuarioId, origemTipo, origemId, origemReferencia));
        String detalhe = "clienteId=" + clienteId + ";valor=" + valorOriginal;
        if (origemTipo != null) detalhe += ";origemTipo=" + origemTipo + ";origemId=" + origemId + ";origemReferencia=" + origemReferencia;
        auditoria.registrar(tenantId, usuarioId, null, filialId, "CRIAR", "CONTA_RECEBER", conta.getId(), detalhe);
        return conta;
    }

    @Transactional public ContaReceber receber(UUID tenantId, UUID usuarioId, UUID contaId) { ContaReceber conta = buscarParaAtualizacao(tenantId, contaId); return registrarRecebimento(tenantId, usuarioId, conta, conta.getSaldoAberto()); }
    @Transactional public ContaReceber receber(UUID tenantId, UUID usuarioId, UUID contaId, BigDecimal valor) { return registrarRecebimento(tenantId, usuarioId, buscarParaAtualizacao(tenantId, contaId), valor); }
    @Transactional public ContaReceber cancelar(UUID tenantId, UUID usuarioId, UUID contaId) { ContaReceber conta = buscarParaAtualizacao(tenantId, contaId); conta.cancelar(); repository.save(conta); auditoria.registrar(tenantId, usuarioId, null, conta.getFilialId(), "CANCELAR", "CONTA_RECEBER", conta.getId(), null); return conta; }

    private ContaReceber buscarParaAtualizacao(UUID tenantId, UUID contaId) { return repository.findByIdAndTenantIdForUpdate(contaId, tenantId).orElseThrow(() -> new RecursoNaoEncontradoException("Conta a receber nao encontrada para o tenant informado")); }
    private ContaReceber registrarRecebimento(UUID tenantId, UUID usuarioId, ContaReceber conta, BigDecimal valor) { conta.receber(valor); ContaReceberRecebimento recebimento = recebimentoRepository.save(new ContaReceberRecebimento(tenantId, conta.getFilialId(), conta.getId(), valor, usuarioId)); repository.save(conta); auditoria.registrar(tenantId, usuarioId, null, conta.getFilialId(), "BAIXAR", "CONTA_RECEBER", conta.getId(), "recebimentoId=" + recebimento.getId() + ";valor=" + valor + ";status=" + conta.getStatus()); return conta; }

    private String normalizarStatus(String status) { if (status == null || status.isBlank()) return null; String n = status.trim().toUpperCase(Locale.ROOT); if (!"ABERTO".equals(n) && !"PARCIAL".equals(n) && !"RECEBIDO".equals(n) && !"CANCELADO".equals(n)) throw new RegraNegocioException("Status de conta a receber invalido"); return n; }
    private void validarPeriodo(LocalDate inicio, LocalDate fim) { if (inicio != null && fim != null && inicio.isAfter(fim)) throw new RegraNegocioException("Vencimento inicial nao pode ser posterior ao vencimento final"); }
    private String normalizarObrigatorio(String valor, String campo) { if (valor == null || valor.isBlank()) throw new RegraNegocioException(campo + " e obrigatorio"); return valor.trim(); }
}
