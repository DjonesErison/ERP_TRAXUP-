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
            Set.of("PENDENTE", "PROCESSANDO", "CONCLUIDO", "FALHOU",
                    "CANCELADO");
    private static final int LIMITE_PADRAO = 100;
    private static final int LIMITE_MAXIMO = 500;

    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;
    private final EscopoFilialContabilidade escopoFilial;

    public SpedExportacaoApplicationService(
            JdbcTemplate jdbc, AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
        this.escopoFilial = new EscopoFilialContabilidade(jdbc);
    }

    @Transactional
    public Exportacao solicitar(UUID tenantId, UUID usuarioId,
                                UUID filialId, String tipo,
                                YearMonth competencia) {
        String tipoNormalizado = normalizarTipo(tipo);
        if (competencia == null)
            throw new IllegalArgumentException("Competencia e obrigatoria");
        if (filialId == null)
            throw new IllegalArgumentException("Filial e obrigatoria");
        escopoFilial.resolver(tenantId, usuarioId, filialId);

        UUID id = UUID.randomUUID();
        int inseridos = jdbc.update("""
                INSERT INTO contabilidade_sped_exportacoes (
                    id, tenant_id, filial_id, tipo, competencia, status,
                    solicitado_por_id
                )
                VALUES (?, ?, ?, ?, ?, 'PENDENTE', ?)
                ON CONFLICT (tenant_id, filial_id, tipo, competencia) DO UPDATE
                SET status = 'PENDENTE',
                    tentativas_processamento = 0,
                    chave_objeto = NULL,
                    hash_sha256 = NULL,
                    versao_layout = NULL,
                    erro_codigo = NULL,
                    concluido_em = NULL,
                    atualizado_em = CURRENT_TIMESTAMP
                WHERE contabilidade_sped_exportacoes.status = 'CANCELADO'
                """, id, tenantId, filialId, tipoNormalizado,
                Date.valueOf(competencia.atDay(1)), usuarioId);

        Exportacao exportacao = buscarPorCompetencia(
                tenantId, filialId, tipoNormalizado, competencia);
        auditoria.registrar(tenantId, usuarioId, null, filialId,
                inseridos == 1 ? "SOLICITAR_SPED" : "REUTILIZAR_SOLICITACAO_SPED",
                "SPED_EXPORTACAO", exportacao.id(),
                "tipo=" + tipoNormalizado + ";competencia=" + competencia
                        + ";filialId=" + filialId);
        return exportacao;
    }

    @Transactional
    public List<Exportacao> listar(UUID tenantId, UUID usuarioId,
                                   String tipo, Integer limite) {
        return listar(tenantId, usuarioId, null, tipo,
                null, null, null, limite);
    }

    @Transactional
    public List<Exportacao> listar(
            UUID tenantId, UUID usuarioId, UUID filialId,
            String tipo, String status,
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
        var escopo = escopoFilial.resolver(
                tenantId, usuarioId, filialId);

        List<Exportacao> exportacoes = jdbc.query("""
                SELECT id, filial_id, tipo, competencia, status, hash_sha256,
                       versao_layout, erro_codigo, criado_em, atualizado_em,
                       concluido_em, retencao_ate
                FROM contabilidade_sped_exportacoes
                WHERE tenant_id = ?
                  AND (CAST(? AS UUID) IS NULL OR filial_id = ?)
                  AND (CAST(? AS BOOLEAN) = TRUE OR filial_id IN (
                      SELECT uf.filial_id
                      FROM usuario_filiais uf
                      WHERE uf.tenant_id = ? AND uf.usuario_id = ?
                  ))
                  AND (CAST(? AS VARCHAR) IS NULL OR tipo = ?)
                  AND (CAST(? AS VARCHAR) IS NULL OR status = ?)
                  AND (CAST(? AS DATE) IS NULL OR competencia >= ?)
                  AND (CAST(? AS DATE) IS NULL OR competencia <= ?)
                ORDER BY competencia DESC, criado_em DESC, id
                LIMIT ?
                """, (rs, n) -> mapear(
                        rs.getObject("id", UUID.class),
                        rs.getObject("filial_id", UUID.class),
                        rs.getString("tipo"),
                        rs.getDate("competencia"),
                        rs.getString("status"),
                        rs.getString("hash_sha256"),
                        rs.getString("versao_layout"),
                        rs.getString("erro_codigo"),
                        rs.getTimestamp("criado_em").toInstant(),
                        rs.getTimestamp("atualizado_em").toInstant(),
                        rs.getTimestamp("concluido_em") == null ? null
                                : rs.getTimestamp("concluido_em").toInstant(),
                        rs.getTimestamp("retencao_ate") == null ? null
                                : rs.getTimestamp("retencao_ate").toInstant()),
                tenantId, filialId, filialId,
                escopo.acessoTotal(), tenantId, usuarioId,
                tipoNormalizado, tipoNormalizado,
                statusNormalizado, statusNormalizado,
                inicio, inicio, fim, fim, limiteEfetivo);

        auditoria.registrar(tenantId, usuarioId, null, filialId,
                "LISTAR_EXPORTACOES_SPED", "SPED_EXPORTACAO",
                UUID.randomUUID(), "tipo=" + tipoNormalizado
                        + ";status=" + statusNormalizado
                        + ";inicio=" + competenciaInicio
                        + ";fim=" + competenciaFim
                        + ";filialId=" + filialId
                        + ";resultados=" + exportacoes.size());
        return exportacoes;
    }

    private Exportacao buscarPorCompetencia(
            UUID tenantId, UUID filialId,
            String tipo, YearMonth competencia) {
        List<Exportacao> encontrados = jdbc.query("""
                SELECT id, filial_id, tipo, competencia, status, hash_sha256,
                       versao_layout, erro_codigo, criado_em, atualizado_em,
                       concluido_em, retencao_ate
                FROM contabilidade_sped_exportacoes
                WHERE tenant_id = ?
                  AND filial_id = ?
                  AND tipo = ?
                  AND competencia = ?
                """, (rs, n) -> mapear(
                        rs.getObject("id", UUID.class),
                        rs.getObject("filial_id", UUID.class),
                        rs.getString("tipo"),
                        rs.getDate("competencia"),
                        rs.getString("status"),
                        rs.getString("hash_sha256"),
                        rs.getString("versao_layout"),
                        rs.getString("erro_codigo"),
                        rs.getTimestamp("criado_em").toInstant(),
                        rs.getTimestamp("atualizado_em").toInstant(),
                        rs.getTimestamp("concluido_em") == null ? null
                                : rs.getTimestamp("concluido_em").toInstant(),
                        rs.getTimestamp("retencao_ate") == null ? null
                                : rs.getTimestamp("retencao_ate").toInstant()),
                tenantId, filialId, tipo,
                Date.valueOf(competencia.atDay(1)));
        if (encontrados.isEmpty())
            throw new RecursoNaoEncontradoException(
                    "Exportacao SPED nao encontrada para o tenant informado");
        return encontrados.getFirst();
    }

    private static Exportacao mapear(
            UUID id, UUID filialId, String tipo,
            Date competencia, String status,
            String hash, String versaoLayout, String erroCodigo,
            Instant criadoEm, Instant atualizadoEm, Instant concluidoEm,
            Instant retencaoAte) {
        return new Exportacao(id, filialId, tipo,
                YearMonth.from(competencia.toLocalDate()), status,
                hash, versaoLayout, erroCodigo, criadoEm, atualizadoEm,
                concluidoEm, retencaoAte);
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
            UUID filialId,
            String tipo,
            YearMonth competencia,
            String status,
            String hashSha256,
            String versaoLayout,
            String erroCodigo,
            Instant criadoEm,
            Instant atualizadoEm,
            Instant concluidoEm,
            Instant retencaoAte) {}
}
