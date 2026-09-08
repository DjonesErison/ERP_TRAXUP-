package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConciliacaoReferenciaApplicationService {
    private final ConciliacaoLancamentoRepository repository;
    private final ContaFinanceiraApplicationService contaFinanceiraService;

    public ConciliacaoReferenciaApplicationService(ConciliacaoLancamentoRepository repository,
                                                    ContaFinanceiraApplicationService contaFinanceiraService) {
        this.repository = repository;
        this.contaFinanceiraService = contaFinanceiraService;
    }

    public ConciliacaoLancamento buscar(UUID tenantId, UUID contaId, String origem, String referenciaExterna) {
        String origemNormalizada = obrigatorio(origem, "Origem").toUpperCase(Locale.ROOT);
        String referenciaNormalizada = obrigatorio(referenciaExterna, "Referencia externa");
        contaFinanceiraService.buscar(tenantId, contaId);
        return repository.findByTenantIdAndContaFinanceiraIdAndOrigemAndReferenciaExterna(
                        tenantId, contaId, origemNormalizada, referenciaNormalizada)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Lancamento de conciliacao nao encontrado para a conta e referencia informadas"));
    }

    private String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException(campo + " e obrigatorio");
        }
        return valor.trim();
    }
}
