package com.traxup.tplug.erp.pdv;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PdvTerminalApplicationService {
    private final PdvTerminalRepository repository;
    private final FilialRepository filialRepository;
    private final AuditoriaApplicationService auditoria;

    public PdvTerminalApplicationService(PdvTerminalRepository repository,
                                         FilialRepository filialRepository,
                                         AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.filialRepository = filialRepository;
        this.auditoria = auditoria;
    }

    public List<PdvTerminal> listar(UUID tenantId, UUID filialId) {
        if (filialId != null && !filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }
        return filialId == null
                ? repository.findAllByTenantIdOrderByNomeAsc(tenantId)
                : repository.findAllByTenantIdAndFilialIdOrderByNomeAsc(tenantId, filialId);
    }

    public PdvTerminal buscar(UUID tenantId, UUID terminalId) {
        return repository.findByIdAndTenantId(terminalId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Terminal PDV nao encontrado para o tenant informado"));
    }

    @Transactional
    public PdvTerminal criar(UUID tenantId, UUID usuarioId, UUID filialId, String codigo, String nome, Integer serie) {
        if (filialId == null || !filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }
        String codigoNormalizado = normalizarCodigo(codigo);
        String nomeNormalizado = normalizarNome(nome);
        if (serie == null || serie <= 0) {
            throw new RegraNegocioException("Serie do terminal deve ser maior que zero");
        }
        if (repository.existsByTenantIdAndCodigo(tenantId, codigoNormalizado)) {
            throw new RegraNegocioException("Codigo de terminal ja existe no tenant");
        }
        if (repository.existsByTenantIdAndFilialIdAndSerie(tenantId, filialId, serie)) {
            throw new RegraNegocioException("Serie de terminal ja existe na filial");
        }
        PdvTerminal terminal = repository.save(new PdvTerminal(tenantId, filialId, codigoNormalizado, nomeNormalizado, serie));
        auditoria.registrar(tenantId, usuarioId, null, filialId, "CRIAR", "PDV_TERMINAL", terminal.getId(),
                "codigo=" + codigoNormalizado + ";serie=" + serie);
        return terminal;
    }

    @Transactional
    public PdvTerminal ativar(UUID tenantId, UUID usuarioId, UUID terminalId) {
        PdvTerminal terminal = buscar(tenantId, terminalId);
        terminal.ativar();
        auditoria.registrar(tenantId, usuarioId, null, terminal.getFilialId(), "ATIVAR", "PDV_TERMINAL", terminal.getId(), null);
        return terminal;
    }

    @Transactional
    public PdvTerminal desativar(UUID tenantId, UUID usuarioId, UUID terminalId) {
        PdvTerminal terminal = buscar(tenantId, terminalId);
        terminal.desativar();
        auditoria.registrar(tenantId, usuarioId, null, terminal.getFilialId(), "DESATIVAR", "PDV_TERMINAL", terminal.getId(), null);
        return terminal;
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) throw new RegraNegocioException("Codigo do terminal e obrigatorio");
        String valor = codigo.trim().toUpperCase(Locale.ROOT);
        if (valor.length() > 64) throw new RegraNegocioException("Codigo do terminal deve ter no maximo 64 caracteres");
        if (!valor.matches("[A-Z0-9._-]+")) {
            throw new RegraNegocioException("Codigo do terminal deve conter apenas letras, numeros, ponto, hifen ou underscore");
        }
        return valor;
    }

    private String normalizarNome(String nome) {
        if (nome == null || nome.isBlank()) throw new RegraNegocioException("Nome do terminal e obrigatorio");
        String valor = nome.trim();
        if (valor.length() > 120) throw new RegraNegocioException("Nome do terminal deve ter no maximo 120 caracteres");
        return valor;
    }
}
