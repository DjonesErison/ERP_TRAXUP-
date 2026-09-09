package com.traxup.tplug.erp.pdv;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.financeiro.ContaFinanceira;
import com.traxup.tplug.erp.financeiro.ContaFinanceiraApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PdvCaixaApplicationService {
    private final PdvCaixaSessaoRepository repository;
    private final PdvTerminalApplicationService terminalService;
    private final ContaFinanceiraApplicationService contaService;
    private final AuditoriaApplicationService auditoria;

    public PdvCaixaApplicationService(PdvCaixaSessaoRepository repository,
                                      PdvTerminalApplicationService terminalService,
                                      ContaFinanceiraApplicationService contaService,
                                      AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.terminalService = terminalService;
        this.contaService = contaService;
        this.auditoria = auditoria;
    }

    public List<PdvCaixaSessao> listar(UUID tenantId, UUID terminalId) {
        terminalService.buscar(tenantId, terminalId);
        return repository.findAllByTenantIdAndTerminalIdOrderByAbertoEmDesc(tenantId, terminalId);
    }

    public PdvCaixaSessao buscarAberta(UUID tenantId, UUID terminalId) {
        terminalService.buscar(tenantId, terminalId);
        return repository.findFirstByTenantIdAndTerminalIdAndStatusOrderByAbertoEmDesc(tenantId, terminalId, "ABERTO")
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nao existe sessao de caixa aberta para o terminal"));
    }

    @Transactional
    public PdvCaixaSessao abrir(UUID tenantId, UUID usuarioId, UUID terminalId, UUID contaFinanceiraId) {
        PdvTerminal terminal = terminalService.buscar(tenantId, terminalId);
        if (!terminal.isAtivo()) throw new IllegalArgumentException("Terminal PDV inativo nao pode abrir caixa");
        if (repository.findFirstByTenantIdAndTerminalIdAndStatusOrderByAbertoEmDesc(tenantId, terminalId, "ABERTO").isPresent()) {
            throw new IllegalArgumentException("Terminal ja possui sessao de caixa aberta");
        }
        ContaFinanceira conta = contaService.buscar(tenantId, contaFinanceiraId);
        if (!conta.isAtivo()) throw new IllegalArgumentException("Conta financeira inativa nao pode ser usada no caixa");
        if (!"CAIXA".equals(conta.getTipo())) throw new IllegalArgumentException("Conta financeira do PDV deve ser do tipo CAIXA");
        if (!terminal.getFilialId().equals(conta.getFilialId())) throw new IllegalArgumentException("Conta financeira deve pertencer a mesma filial do terminal");

        PdvCaixaSessao sessao = repository.save(new PdvCaixaSessao(
                tenantId, terminal.getFilialId(), terminalId, contaFinanceiraId, usuarioId, conta.getSaldo()));
        auditoria.registrar(tenantId, usuarioId, null, terminal.getFilialId(), "ABRIR", "PDV_CAIXA", sessao.getId(),
                "terminalId=" + terminalId + ";contaFinanceiraId=" + contaFinanceiraId + ";saldoAbertura=" + conta.getSaldo());
        return sessao;
    }

    @Transactional
    public PdvCaixaSessao fechar(UUID tenantId, UUID usuarioId, UUID sessaoId,
                                 BigDecimal saldoInformado, String observacao) {
        PdvCaixaSessao sessao = repository.findByIdAndTenantId(sessaoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sessao de caixa nao encontrada para o tenant informado"));
        if (!"ABERTO".equals(sessao.getStatus())) throw new IllegalArgumentException("Sessao de caixa ja esta fechada");
        ContaFinanceira conta = contaService.buscar(tenantId, sessao.getContaFinanceiraId());
        sessao.fechar(usuarioId, conta.getSaldo(), saldoInformado, observacao);
        repository.save(sessao);
        auditoria.registrar(tenantId, usuarioId, null, sessao.getFilialId(), "FECHAR", "PDV_CAIXA", sessao.getId(),
                "terminalId=" + sessao.getTerminalId() + ";saldoSistema=" + conta.getSaldo() + ";saldoInformado=" + saldoInformado
                        + ";diferenca=" + sessao.getDiferencaFechamento());
        return sessao;
    }
}
