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
public class FiscalFalhasPendentesApplicationService {
    private final JdbcTemplate jdbc;

    public FiscalFalhasPendentesApplicationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Resultado listar(UUID tenantId, Integer limiteSolicitado) {
        int limite = normalizarLimite(limiteSolicitado);
        List<Item> itens = jdbc.query("""
                SELECT tentativa_id, documento_id, numero, tentativa_status,
                       falha_id, etapa, tipo_erro, ocorrida_em
                FROM (
                    SELECT DISTINCT ON (te.id)
                           te.id AS tentativa_id, te.documento_id, te.numero,
                           te.status AS tentativa_status, f.id AS falha_id,
                           f.etapa, f.tipo_erro, f.ocorrida_em
                    FROM fiscal_tentativas_emissao te
                    JOIN fiscal_tentativa_falhas f
                      ON f.tenant_id = te.tenant_id AND f.tentativa_id = te.id
                     AND f.resolvida_em IS NULL
                    WHERE te.tenant_id = ?
                    ORDER BY te.id, f.ocorrida_em DESC, f.id DESC
                ) ultimas
                ORDER BY ocorrida_em DESC, tentativa_id DESC
                LIMIT ?
                """, (rs, n) -> new Item(
                        rs.getObject("tentativa_id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getInt("numero"), rs.getString("tentativa_status"),
                        rs.getObject("falha_id", UUID.class), rs.getString("etapa"),
                        rs.getString("tipo_erro"),
                        instante(rs.getTimestamp("ocorrida_em"))),
                tenantId, limite);
        return new Resultado(itens.size(), limite, itens);
    }

    static int normalizarLimite(Integer limite) {
        if (limite == null) return 50;
        if (limite < 1 || limite > 100)
            throw new IllegalArgumentException("Limite deve estar entre 1 e 100");
        return limite;
    }

    private static Instant instante(Timestamp valor) {
        return valor == null ? null : valor.toInstant();
    }

    public record Resultado(int totalRetornado, int limite, List<Item> itens) {}

    public record Item(UUID tentativaId, UUID documentoId, int tentativaNumero,
                       String tentativaStatus, UUID falhaId, String etapa,
                       String tipoErro, Instant ocorridaEm) {}
}
