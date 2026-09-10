package com.traxup.tplug.erp.fiscal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FiscalTentativaFalhaApplicationService {
    private final JdbcTemplate jdbc;

    public FiscalTentativaFalhaApplicationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void prepararRetomada(UUID tenantId, UUID tentativaId) {
        int retomadas = jdbc.update("""
                UPDATE fiscal_tentativas_emissao
                SET status = 'EM_PROCESSAMENTO', concluida_em = NULL
                WHERE tenant_id = ? AND id = ? AND status = 'FALHOU'
                """, tenantId, tentativaId);
        if (retomadas == 1) {
            jdbc.update("""
                    UPDATE fiscal_tentativa_falhas
                    SET resolvida_em = CURRENT_TIMESTAMP
                    WHERE tenant_id = ? AND tentativa_id = ? AND resolvida_em IS NULL
                    """, tenantId, tentativaId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(UUID tenantId, UUID tentativaId, Etapa etapa,
                          RuntimeException erro) {
        boolean existe = Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM fiscal_tentativas_emissao
                    WHERE tenant_id = ? AND id = ?
                )
                """, Boolean.class, tenantId, tentativaId));
        if (!existe) return;
        jdbc.update("""
                UPDATE fiscal_tentativas_emissao
                SET status = 'FALHOU',
                    iniciada_em = COALESCE(iniciada_em, CURRENT_TIMESTAMP),
                    concluida_em = CURRENT_TIMESTAMP
                WHERE tenant_id = ? AND id = ? AND status <> 'CONCLUIDA'
                """, tenantId, tentativaId);
        String tipo = erro.getClass().getSimpleName();
        UUID id = UUID.nameUUIDFromBytes((tentativaId + ":" + etapa + ":"
                + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));
        jdbc.update("""
                INSERT INTO fiscal_tentativa_falhas
                    (id, tenant_id, tentativa_id, etapa, tipo_erro)
                VALUES (?, ?, ?, ?, ?)
                """, id, tenantId, tentativaId, etapa.name(), tipo);
        jdbc.update("""
                UPDATE fiscal_tentativas_emissao
                SET status = 'FALHOU',
                    iniciada_em = COALESCE(iniciada_em, CURRENT_TIMESTAMP),
                    concluida_em = CURRENT_TIMESTAMP
                WHERE tenant_id = ? AND id = ?
                  AND status IN ('CRIADA', 'EM_PROCESSAMENTO')
                """, tenantId, tentativaId);
    }

    public enum Etapa { XML, ASSINATURA, TRANSMISSAO, PROCESSADO }
    public enum Etapa {
        XML, ASSINATURA, TRANSMISSAO, PROCESSADO
    }
}
