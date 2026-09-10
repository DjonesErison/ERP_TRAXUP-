package com.traxup.tplug.erp.fiscal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FiscalTentativasParadasApplicationService {
    private final JdbcTemplate jdbc;

    public FiscalTentativasParadasApplicationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Resultado listar(UUID tenantId, Integer minutosSolicitados) {
        int minutos = normalizarMinutos(minutosSolicitados);
        List<Item> itens = jdbc.query("""
                SELECT te.id, te.documento_id, te.numero, te.iniciada_em,
                       x.id AS xml_id, a.id AS assinatura_id,
                       tr.id AS transmissao_id, p.id AS processado_id
                FROM fiscal_tentativas_emissao te
                LEFT JOIN fiscal_documentos_xml x
                  ON x.tenant_id = te.tenant_id AND x.tentativa_id = te.id
                LEFT JOIN fiscal_documentos_assinaturas a
                  ON a.tenant_id = te.tenant_id AND a.tentativa_id = te.id
                LEFT JOIN fiscal_transmissoes tr
                  ON tr.tenant_id = te.tenant_id AND tr.tentativa_id = te.id
                LEFT JOIN fiscal_documentos_processados p
                  ON p.tenant_id = te.tenant_id AND p.tentativa_id = te.id
                WHERE te.tenant_id = ?
                  AND te.status = 'EM_PROCESSAMENTO'
                  AND te.iniciada_em < CURRENT_TIMESTAMP - (? * INTERVAL '1 minute')
                ORDER BY te.iniciada_em
                LIMIT 100
                """, (rs, n) -> {
                    UUID xmlId = rs.getObject("xml_id", UUID.class);
                    UUID assinaturaId = rs.getObject("assinatura_id", UUID.class);
                    UUID transmissaoId = rs.getObject("transmissao_id", UUID.class);
                    UUID processadoId = rs.getObject("processado_id", UUID.class);
                    return new Item(rs.getObject("id", UUID.class),
                            rs.getObject("documento_id", UUID.class),
                            rs.getInt("numero"),
                            etapa(xmlId, assinaturaId, transmissaoId, processadoId),
                            instante(rs.getTimestamp("iniciada_em")),
                            xmlId, assinaturaId, transmissaoId, processadoId);
                }, tenantId, minutos);
        return new Resultado(Instant.now(), minutos, itens.size(), itens);
    }

    static int normalizarMinutos(Integer minutos) {
        if (minutos == null) return 15;
        if (minutos < 5 || minutos > 1440)
            throw new IllegalArgumentException(
                    "Tempo de processamento deve estar entre 5 e 1440 minutos");
        return minutos;
    }

    static String etapa(UUID xmlId, UUID assinaturaId,
                        UUID transmissaoId, UUID processadoId) {
        if (processadoId != null) return "PROCESSADO";
        if (transmissaoId != null) return "TRANSMITIDO";
        if (assinaturaId != null) return "ASSINADO";
        if (xmlId != null) return "XML_GERADO";
        return "INICIADA";
    }

    private static Instant instante(Timestamp valor) {
        return valor == null ? null : valor.toInstant();
    }

    public record Resultado(Instant consultadoEm, int minutosSemConclusao,
                            int totalRetornado, List<Item> itens) {}

    public record Item(UUID tentativaId, UUID documentoId, int tentativaNumero,
                       String ultimaEtapa, Instant iniciadaEm, UUID xmlId,
                       UUID assinaturaId, UUID transmissaoId, UUID processadoId) {}
}
