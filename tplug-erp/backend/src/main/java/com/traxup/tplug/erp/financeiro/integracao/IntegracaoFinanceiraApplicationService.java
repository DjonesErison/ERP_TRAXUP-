package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.financeiro.ContaFinanceira;
import com.traxup.tplug.erp.financeiro.ContaFinanceiraApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class IntegracaoFinanceiraApplicationService {
    private final IntegracaoFinanceiraRepository repository;
    private final ContaFinanceiraApplicationService contaFinanceiraService;
    private final AuditoriaApplicationService auditoria;

    public IntegracaoFinanceiraApplicationService(IntegracaoFinanceiraRepository repository,
                                                   ContaFinanceiraApplicationService contaFinanceiraService,
                                                   AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.contaFinanceiraService = contaFinanceiraService;
        this.auditoria = auditoria;
    }

    public List<IntegracaoFinanceira> listar(UUID tenantId, UUID contaId) {
        contaFinanceiraService.buscar(tenantId, contaId);
        return repository.findAllByTenantIdAndContaFinanceiraIdOrderByProvedorAsc(tenantId, contaId);
    }

    @Transactional
    public IntegracaoFinanceira criar(UUID tenantId, UUID usuarioId, UUID contaId,
                                      String provedor, String identificadorExterno) {
        ContaFinanceira conta = contaFinanceiraService.buscar(tenantId, contaId);
        String provedorNormalizado = obrigatorio(provedor).toUpperCase(Locale.ROOT);
        if (!conta.isAtivo()) throw new RecursoConflitanteException("Conta financeira inativa nao pode receber integracao");
        if (repository.existsByTenantIdAndContaFinanceiraIdAndProvedor(tenantId, contaId, provedorNormalizado)) {
            throw new RecursoConflitanteException("Provedor ja configurado para esta conta financeira");
        }
        IntegracaoFinanceira integracao = repository.save(new IntegracaoFinanceira(
                tenantId, conta.getFilialId(), contaId, provedorNormalizado, identificadorExterno, usuarioId));
        auditoria.registrar(tenantId, usuarioId, null, conta.getFilialId(),
                "CRIAR", "INTEGRACAO_FINANCEIRA", integracao.getId(),
                "contaId=" + contaId + ";provedor=" + provedorNormalizado);
        return integracao;
    }

    @Transactional
    public IntegracaoFinanceira registrarSincronizacao(UUID tenantId, UUID usuarioId, UUID integracaoId,
                                                       String checkpoint, Instant sincronizadoEm) {
        IntegracaoFinanceira integracao = repository.findByIdAndTenantId(integracaoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Integracao financeira nao encontrada para o tenant informado"));
        if (!integracao.isAtivo()) throw new RecursoConflitanteException("Integracao financeira inativa nao pode ser sincronizada");
        integracao.registrarSincronizacao(checkpoint, sincronizadoEm);
        repository.save(integracao);
        auditoria.registrar(tenantId, usuarioId, null, integracao.getFilialId(),
                "SINCRONIZAR", "INTEGRACAO_FINANCEIRA", integracao.getId(),
                "provedor=" + integracao.getProvedor() + ";sincronizadoEm=" + sincronizadoEm);
        return integracao;
    }

    @Transactional
    public IntegracaoFinanceira desativar(UUID tenantId, UUID usuarioId, UUID integracaoId) {
        IntegracaoFinanceira integracao = repository.findByIdAndTenantId(integracaoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Integracao financeira nao encontrada para o tenant informado"));
        integracao.desativar();
        repository.save(integracao);
        auditoria.registrar(tenantId, usuarioId, null, integracao.getFilialId(),
                "DESATIVAR", "INTEGRACAO_FINANCEIRA", integracao.getId(),
                "provedor=" + integracao.getProvedor());
        return integracao;
    }

    private String obrigatorio(String valor) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Provedor e obrigatorio");
        return valor.trim();
    }
}
