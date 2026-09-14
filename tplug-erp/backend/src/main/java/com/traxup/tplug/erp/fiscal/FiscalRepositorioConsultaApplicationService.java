package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.contabilidade.EscopoFilialContabilidade;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FiscalRepositorioConsultaApplicationService {
    private static final Set<String> STATUS = Set.of(
            "PENDENTE", "ARQUIVANDO", "ARQUIVADO", "FALHOU");
    private final JdbcTemplate jdbc;
    private final EscopoFilialContabilidade escopoFilial;

    public FiscalRepositorioConsultaApplicationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.escopoFilial = new EscopoFilialContabilidade(jdbc);
    }

    public Resultado listar(UUID tenantId, UUID usuarioId, UUID filialId,
                            LocalDate inicioSolicitado,
                            LocalDate fimSolicitado, String statusSolicitado,
                            Integer limiteSolicitado) {
        var escopo = escopoFilial.resolver(tenantId, usuarioId, filialId);
        Filtros filtros = normalizar(inicioSolicitado, fimSolicitado,
                statusSolicitado, limiteSolicitado);
        List<Item> itens = jdbc.query("""
                SELECT a.id, a.documento_id, a.tentativa_id, a.tipo,
                       a.hash_sha256, a.status, a.tentativas_envio,
                       a.criado_em, a.arquivado_em, a.retencao_ate,
                       d.filial_id, d.modelo, d.serie, d.numero
                FROM fiscal_arquivos a
                JOIN fiscal_documentos d
                  ON d.tenant_id = a.tenant_id AND d.id = a.documento_id
                WHERE a.tenant_id = ?
                  AND a.criado_em::date BETWEEN ? AND ?
                  AND (CAST(? AS UUID) IS NULL OR d.filial_id = ?)
                  AND (CAST(? AS BOOLEAN) = TRUE OR d.filial_id IN (
                      SELECT uf.filial_id
                      FROM usuario_filiais uf
                      WHERE uf.tenant_id = ? AND uf.usuario_id = ?
                  ))
                  AND (CAST(? AS VARCHAR) IS NULL OR a.status = ?)
                ORDER BY a.criado_em DESC, a.id DESC
                LIMIT ?
                """, (rs, n) -> new Item(
                        rs.getObject("id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("filial_id", UUID.class),
                        rs.getObject("tentativa_id", UUID.class),
                        rs.getString("modelo"),
                        rs.getObject("serie", Integer.class),
                        rs.getObject("numero", Long.class),
                        rs.getString("tipo"), rs.getString("hash_sha256"),
                        rs.getString("status"), rs.getInt("tentativas_envio"),
                        instante(rs.getTimestamp("criado_em")),
                        instante(rs.getTimestamp("arquivado_em")),
                        rs.getObject("retencao_ate", LocalDate.class),
                        "ARQUIVADO".equals(rs.getString("status"))),
                tenantId, filtros.inicio(), filtros.fim(),
                filialId, filialId,
                escopo.acessoTotal(), tenantId, usuarioId,
                filtros.status(), filtros.status(), filtros.limite());
        return new Resultado(filtros.inicio(), filtros.fim(), filtros.status(),
                filtros.limite(), itens.size(), itens);
    }

    static Filtros normalizar(LocalDate inicio, LocalDate fim,
                              String status, Integer limite) {
        LocalDate fimNormalizado = fim == null ? LocalDate.now() : fim;
        LocalDate inicioNormalizado = inicio == null
                ? fimNormalizado.minusDays(30) : inicio;
        if (inicioNormalizado.isAfter(fimNormalizado))
            throw new IllegalArgumentException(
                    "Data inicial deve ser anterior ou igual a data final");
        if (ChronoUnit.DAYS.between(inicioNormalizado, fimNormalizado) > 366)
            throw new IllegalArgumentException(
                    "Periodo de consulta deve ter no maximo 366 dias");
        String statusNormalizado = status == null || status.isBlank()
                ? null : status.trim().toUpperCase();
        if (statusNormalizado != null && !STATUS.contains(statusNormalizado))
            throw new IllegalArgumentException("Status de arquivo fiscal invalido");
        int limiteNormalizado = limite == null ? 50 : limite;
        if (limiteNormalizado < 1 || limiteNormalizado > 200)
            throw new IllegalArgumentException(
                    "Limite deve estar entre 1 e 200");
        return new Filtros(inicioNormalizado, fimNormalizado,
                statusNormalizado, limiteNormalizado);
    }

    private static Instant instante(Timestamp valor) {
        return valor == null ? null : valor.toInstant();
    }

    record Filtros(LocalDate inicio, LocalDate fim, String status, int limite) {}

    public record Resultado(LocalDate inicio, LocalDate fim, String status,
                            int limite, int totalRetornado, List<Item> itens) {}

    public record Item(UUID arquivoId, UUID documentoId, UUID filialId,
                       UUID tentativaId,
                       String modelo, Integer serie, Long numero, String tipo,
                       String hashSha256, String status, int tentativasEnvio,
                       Instant criadoEm, Instant arquivadoEm, LocalDate retencaoAte,
                       boolean downloadDisponivel) {}
}
