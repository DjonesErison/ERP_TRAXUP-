package com.traxup.tplug.erp.pdv;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PdvVendaSincronizacaoApplicationService {
    private final PdvVendaSincronizacaoRepository repository;
    private final PdvTerminalApplicationService terminalService;
    private final AuditoriaApplicationService auditoria;

    public PdvVendaSincronizacaoApplicationService(PdvVendaSincronizacaoRepository repository,
                                                   PdvTerminalApplicationService terminalService,
                                                   AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.terminalService = terminalService;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado sincronizar(UUID tenantId, UUID usuarioId, UUID terminalId, UUID operacaoLocalId,
                                 Long numeroLocal, String checksum, Instant ocorridoEm) {
        if (terminalId == null || operacaoLocalId == null) {
            throw new RegraNegocioException("Terminal e operacao local sao obrigatorios");
        }
        if (numeroLocal == null || numeroLocal <= 0) {
            throw new RegraNegocioException("Numero local deve ser maior que zero");
        }
        if (ocorridoEm == null) {
            throw new RegraNegocioException("Data da operacao local e obrigatoria");
        }
        String checksumNormalizado = normalizarChecksum(checksum);
        PdvTerminal terminal = terminalService.buscar(tenantId, terminalId);
        if (!terminal.isAtivo()) {
            throw new RegraNegocioException("Terminal PDV precisa estar ativo para sincronizar");
        }

        var existente = repository.findByTenantIdAndTerminalIdAndOperacaoLocalId(tenantId, terminalId, operacaoLocalId);
        if (existente.isPresent()) {
            PdvVendaSincronizacao sync = existente.get();
            if (!sync.corresponde(numeroLocal, checksumNormalizado)) {
                throw new RegraNegocioException("Operacao local ja sincronizada com conteudo diferente");
            }
            return new Resultado(sync, true);
        }

        if (repository.existsByTenantIdAndFilialIdAndSerieAndNumeroLocal(
                tenantId, terminal.getFilialId(), terminal.getSerie(), numeroLocal)) {
            throw new RegraNegocioException("Numero local ja utilizado para a serie deste terminal");
        }

        PdvVendaSincronizacao sync = repository.save(new PdvVendaSincronizacao(
                tenantId, terminal.getFilialId(), terminalId, operacaoLocalId,
                terminal.getSerie(), numeroLocal, checksumNormalizado, ocorridoEm));
        auditoria.registrar(tenantId, usuarioId, null, terminal.getFilialId(), "SINCRONIZAR", "PDV_VENDA", sync.getId(),
                "terminalId=" + terminalId + ";serie=" + terminal.getSerie() + ";numeroLocal=" + numeroLocal);
        return new Resultado(sync, false);
    }

    private String normalizarChecksum(String checksum) {
        if (checksum == null || checksum.isBlank()) throw new RegraNegocioException("Checksum e obrigatorio");
        String valor = checksum.trim().toLowerCase(Locale.ROOT);
        if (!valor.matches("[0-9a-f]{64}")) throw new RegraNegocioException("Checksum deve ser SHA-256 hexadecimal");
        return valor;
    }

    public record Resultado(PdvVendaSincronizacao sincronizacao, boolean repetida) {}
}
