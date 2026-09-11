package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class SpedExportacaoApplicationService {
    private static final Set<String> TIPOS =
            Set.of("EFD_ICMS_IPI", "EFD_CONTRIBUICOES");
    private static final Set<String> STATUS =
            Set.of("PENDENTE", "PROCESSANDO", "CONCLUIDO", "FALHOU");
    private static final int LIMITE_PADRAO = 100;
    private static final int LIMITE_MAXIMO = 500;

    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;

    public SpedExportacaoApplicationService(
            JdbcTemplate jdbc, AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    @Transactional
    public Exportacao solicitar(UUID tenantId, UUID usuarioId,
                                String tipo, YearMonth competencia) {
        String tipoNormalizado = normalizarTipo(tipo);
        if (competencia == null)
            throw new IllegalArgumentException("Competencia e obrigatoria");

        UUID id = UUID.randomUUID();
        int inseridos = jdbc.update("""
                INSERT INTO contabilidade_sped_exportacoes (
                    id, tenant_id, tipo, competencia, status,
                    solicitado_por_id
                )
                VALUES (?, ?, ?, ?, 'PENDENTE', ?)
                ON CONFLICT (tenant_id, tipo, competencia) DO NOTHING
                """, id, tenantId, tipoNormalizado,
                Date.valueOf(competencia.atDay(1)), usuarioId);

        Exportacao exportacao = buscarPorCompetencia(
                tenantId, tipoNormalizado, competencia);
        auditoria.registrar(tenantId, usuarioId, null, null,
                inseridos == 1 ? "SOLICITAR_SPED" : "REUTILIZAR_SOLICITACAO_SPED",
                "SPED_EXPORTACAO", exportacao.id(),
                "tipo=" + tipoNormalizado + ";competencia=" + competencia);
        return exportacao;
    }

    @Transactional
    public List<Exportacao> listar(UUID tenantId, UUID usuarioId,
                                   String tipo, Integer limite) {
        return listar(tenantId, usuarioId, tipo, null, null, null, limite);
    }

    @Transactional
    public List<Exportacao> listar(
            UUID tenantId, UUID usuarioId, String tipo, String status,
            YearMonth competenciaInicio, YearMonth competenciaFim,
            Integer limite) {
        String tipoNormalizado =
                tipo == null || tipo.isBlank() ? null : normalizarTipo(tipo);
        String statusNormalizado = normalizarStatusOpcional(status);
        validarIntervalo(competenciaInicio, competenciaFim);
        Date inicio = competenciaInicio == null ? null
                : Date.valueOf(competenciaInicio.atDay(1));
        Date fim = competenciaFim == null ? null
                : Date.valueOf(competenciaFim.atDay(1));
        int limiteEfetivo = validarLimite(limite);

        List<Exportacao> exportacoes = jdbc.query("""
                SELECT id, tipo, competencia, status, hash_sha256,
                       versao_layout, erro_codigo, criado_em, atualizado_em,
                       concluido_em
                FROM contabilidade_sped_exportacoes
                WHERE tenant_id = ?
                  AND (CAST(? AS VARCHAR) IS NULL OR tipo = ?)
                  AND (CAST(? AS VARCHAR) IS NULL OR status = ?)
                  AND (CAST(? AS DATE) IS NULL OR competencia >= ?)
                  AND (CAST(? AS DATE) IS NULL OR competencia <= ?)
                ORDER BY competencia DESC, criado_em DESC, id
                LIMIT ?
                """, (rs, n) -> mapear(
                        rs.getObject("id", UUID.class),
                        rs.getString("tipo"),
                        rs.getDate("competencia"),
                        rs.getString("status"),
                        rs.getString("hash_sha256"),
                        rs.getString("versao_layout"),
                        rs.getString("erro_codigo"),
                        rs.getTimestamp("criado_em").toInstant(),
                        rs.getTimestamp("atualizado_em").toInstant(),
                        rs.getTimestamp("concluido_em") == null ? null
                                : rs.getTimestamp("concluido_em").toInstant()),
                tenantId,
                tipoNormalizado, tipoNormalizado,
                statusNormalizado, statusNormalizado,
                inicio, inicio, fim, fim, limiteEfetivo);

        auditoria.registrar(tenantId, usuarioId, null, null,
                "LISTAR_EXPORTACOES_SPED", "SPED_EXPORTACAO",
                UUID.randomUUID(), "tipo=" + tipoNormalizado
                        + ";status=" + statusNormalizado
                        + ";inicio=" + competenciaInicio
                        + ";fim=" + competenciaFim
                        + ";resultados=" + exportacoes.size());
        return exportacoes;
    }

    private Exportacao buscarPorCompetencia(
            UUID tenantId, String tipo, YearMonth competencia) {
        List<Exportacao> encontrados = jdbc.query("""
                SELECT id, tipo, competencia, status, hash_sha256,
                       versao_layout, erro_codigo, criado_em, atualizado_em,
                       concluido_em
                FROM contabilidade_sped_exportacoes
                WHERE tenant_id = ?
                  AND tipo = ?
                  AND competencia = ?
                """, (rs, n) -> mapear(
                        rs.getObject("id", UUID.class),
                        rs.getString("tipo"),
                        rs.getDate("competencia"),
                        rs.getString("status"),
                        rs.getString("hash_sha256"),
                        rs.getString("versao_layout"),
                        rs.getString("erro_codigo"),
                        rs.getTimestamp("criado_em").toInstant(),
                        rs.getTimestamp("atualizado_em").toInstant(),
                        rs.getTimestamp("concluido_em") == null ? null
                                : rs.getTimestamp("concluido_em").toInstant()),
                tenantId, tipo, Date.valueOf(competencia.atDay(1)));
        if (encontrados.isEmpty())
            throw new RecursoNaoEncontradoException(
                    "Exportacao SPED nao encontrada para o tenant informado");
        return encontrados.getFirst();
    }

    private static Exportacao mapear(
            UUID id, String tipo, Date competencia, String status,
            String hash, String versaoLayout, String erroCodigo,
            Instant criadoEm, Instant atualizadoEm, Instant concluidoEm) {
        return new Exportacao(id, tipo,
                YearMonth.from(competencia.toLocalDate()), status,
                hash, versaoLayout, erroCodigo, criadoEm, atualizadoEm,
                concluidoEm);
    }

    static String normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank())
            throw new IllegalArgumentException("Tipo de SPED e obrigatorio");
        String normalizado = tipo.trim().toUpperCase(Locale.ROOT);
        if (!TIPOS.contains(normalizado))
            throw new IllegalArgumentException(
                    "Tipo deve ser EFD_ICMS_IPI ou EFD_CONTRIBUICOES");
        return normalizado;
    }

    static String normalizarStatusOpcional(String status) {
        if (status == null || status.isBlank()) return null;
        String normalizado = status.trim().toUpperCase(Locale.ROOT);
        if (!STATUS.contains(normalizado))
            throw new IllegalArgumentException(
                    "Status de exportacao SPED invalido");
        return normalizado;
    }

    static void validarIntervalo(YearMonth inicio, YearMonth fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim))
            throw new IllegalArgumentException(
                    "Competencia inicial deve ser anterior ou igual a final");
    }

    static int validarLimite(Integer limite) {
        int valor = limite == null ? LIMITE_PADRAO : limite;
        if (valor < 1 || valor > LIMITE_MAXIMO)
            throw new IllegalArgumentException(
                    "Limite deve estar entre 1 e 500");
        return valor;
    }

    public record Exportacao(
            UUID id,
            String tipo,
            YearMonth competencia,
            String status,
            String hashSha256,
            String versaoLayout,
            String erroCodigo,
            Instant criadoEm,
            Instant atualizadoEm,
            Instant concluidoEm) {}
}
