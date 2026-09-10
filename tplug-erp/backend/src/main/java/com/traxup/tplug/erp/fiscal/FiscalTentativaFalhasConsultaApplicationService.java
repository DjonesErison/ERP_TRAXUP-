package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FiscalTentativaFalhasConsultaApplicationService {
    private final JdbcTemplate jdbc;

    public FiscalTentativaFalhasConsultaApplicationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Historico buscar(UUID tenantId, UUID tentativaId) {
        Tentativa tentativa = jdbc.query("""
                SELECT id, documento_id, numero, status
                FROM fiscal_tentativas_emissao
                WHERE tenant_id = ? AND id = ?
                """, (rs, n) -> new Tentativa(
                        rs.getObject("id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getInt("numero"), rs.getString("status")),
                tenantId, tentativaId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Tentativa fiscal nao encontrada para o tenant"));

        List<Falha> falhas = jdbc.query("""
                SELECT id, etapa, tipo_erro, ocorrida_em, resolvida_em
                FROM fiscal_tentativa_falhas
                WHERE tenant_id = ? AND tentativa_id = ?
                ORDER BY ocorrida_em DESC, id DESC
                """, (rs, n) -> {
                    Instant resolvidaEm = instante(rs.getTimestamp("resolvida_em"));
                    return new Falha(rs.getObject("id", UUID.class),
                            rs.getString("etapa"), rs.getString("tipo_erro"),
                            situacao(resolvidaEm), instante(rs.getTimestamp("ocorrida_em")),
                            resolvidaEm);
                }, tenantId, tentativaId);

        long pendentes = falhas.stream()
                .filter(f -> "PENDENTE".equals(f.situacao())).count();
        return new Historico(tentativa.id(), tentativa.documentoId(),
                tentativa.numero(), tentativa.status(), falhas.size(), pendentes, falhas);
    }

    static String situacao(Instant resolvidaEm) {
        return resolvidaEm == null ? "PENDENTE" : "RESOLVIDA";
    }

    private static Instant instante(Timestamp valor) {
        return valor == null ? null : valor.toInstant();
    }

    record Tentativa(UUID id, UUID documentoId, int numero, String status) {}

    public record Historico(UUID tentativaId, UUID documentoId, int tentativaNumero,
                            String tentativaStatus, int totalFalhas,
                            long falhasPendentes, List<Falha> falhas) {}

    public record Falha(UUID id, String etapa, String tipoErro, String situacao,
                        Instant ocorridaEm, Instant resolvidaEm) {}
}
