package com.traxup.tplug.erp.auditoria;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuditoriaApplicationService {

    private final AuditoriaRepository auditoriaRepository;
    private final TenantRepository tenantRepository;

    public AuditoriaApplicationService(
            AuditoriaRepository auditoriaRepository,
            TenantRepository tenantRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public AuditoriaEvento registrar(
            UUID tenantId,
            UUID usuarioId,
            UUID empresaId,
            UUID filialId,
            String operacao,
            String entidade,
            UUID entidadeId,
            String detalhes) {
        validarTenant(tenantId);
        String operacaoNormalizada = normalizarObrigatorio(operacao, "Operacao obrigatoria");
        String entidadeNormalizada = normalizarObrigatorio(entidade, "Entidade obrigatoria");

        AuditoriaEvento evento = new AuditoriaEvento(
                UUID.randomUUID(),
                tenantId,
                usuarioId,
                empresaId,
                filialId,
                operacaoNormalizada,
                entidadeNormalizada,
                entidadeId,
                detalhes,
                OffsetDateTime.now(ZoneOffset.UTC));

        auditoriaRepository.inserir(evento);
        return evento;
    }

    public List<AuditoriaEvento> listarPorTenant(UUID tenantId) {
        validarTenant(tenantId);
        return auditoriaRepository.listarPorTenant(tenantId);
    }

    private void validarTenant(UUID tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new RecursoNaoEncontradoException("Tenant nao encontrado");
        }
    }

    private String normalizarObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim().toUpperCase();
    }
}
