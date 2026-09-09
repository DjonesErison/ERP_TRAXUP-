package com.traxup.tplug.erp.pdv;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PdvConfiguracaoApplicationService {
    private static final Set<String> TAMANHOS = Set.of("PEQUENA", "MEDIA", "GRANDE");
    private final PdvConfiguracaoRepository repository;
    private final PdvTerminalApplicationService terminalService;
    private final AuditoriaApplicationService auditoria;

    public PdvConfiguracaoApplicationService(PdvConfiguracaoRepository repository,
                                             PdvTerminalApplicationService terminalService,
                                             AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.terminalService = terminalService;
        this.auditoria = auditoria;
    }

    public PdvConfiguracao buscar(UUID tenantId, UUID terminalId) {
        PdvTerminal terminal = terminalService.buscar(tenantId, terminalId);
        return repository.findByTenantIdAndTerminalId(tenantId, terminalId)
                .orElseGet(() -> new PdvConfiguracao(tenantId, terminal.getFilialId(), terminalId));
    }

    @Transactional
    public PdvConfiguracao atualizar(UUID tenantId, UUID usuarioId, UUID terminalId,
                                     boolean exigirJustificativa, boolean exigirAutorizacao,
                                     String tamanhoImpressao, boolean imprimirCaixa, boolean imprimirCozinha) {
        PdvTerminal terminal = terminalService.buscar(tenantId, terminalId);
        String tamanho = normalizarTamanho(tamanhoImpressao);
        PdvConfiguracao config = repository.findByTenantIdAndTerminalId(tenantId, terminalId)
                .orElseGet(() -> new PdvConfiguracao(tenantId, terminal.getFilialId(), terminalId));
        config.atualizar(exigirJustificativa, exigirAutorizacao, tamanho, imprimirCaixa, imprimirCozinha);
        config = repository.save(config);
        auditoria.registrar(tenantId, usuarioId, null, terminal.getFilialId(), "ATUALIZAR", "PDV_CONFIGURACAO",
                config.getId(), "terminalId=" + terminalId + ";tamanhoImpressao=" + tamanho);
        return config;
    }

    private String normalizarTamanho(String tamanho) {
        if (tamanho == null || tamanho.isBlank()) throw new RegraNegocioException("Tamanho de impressao e obrigatorio");
        String valor = tamanho.trim().toUpperCase(Locale.ROOT);
        if (!TAMANHOS.contains(valor)) throw new RegraNegocioException("Tamanho de impressao invalido");
        return valor;
    }
}
