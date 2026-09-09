package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FiscalNumeracaoApplicationService {
    private final JdbcTemplate jdbc;
    private final FiscalPerfilFilialRepository perfis;
    private final FiscalValidacaoApplicationService validacao;
    private final AuditoriaApplicationService auditoria;

    public FiscalNumeracaoApplicationService(JdbcTemplate jdbc,
                                             FiscalPerfilFilialRepository perfis,
                                             FiscalValidacaoApplicationService validacao,
                                             AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.perfis = perfis;
        this.validacao = validacao;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado numerar(UUID tenantId, UUID usuarioId, UUID documentoId) {
        Documento documento = jdbc.query("""
                SELECT id, filial_id, modelo, ambiente, serie, numero, numerado_em
                FROM fiscal_documentos
                WHERE tenant_id = ? AND id = ?
                FOR UPDATE
                """, (rs, n) -> new Documento(
                        rs.getObject("id", UUID.class),
                        rs.getObject("filial_id", UUID.class),
                        rs.getString("modelo"),
                        rs.getString("ambiente"),
                        rs.getObject("serie", Integer.class),
                        rs.getObject("numero", Long.class),
                        rs.getTimestamp("numerado_em") == null
                                ? null : rs.getTimestamp("numerado_em").toInstant()),
                tenantId, documentoId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Documento fiscal nao encontrado para o tenant informado"));

        if (documento.numero() != null) {
            return new Resultado(documento.id(), documento.modelo(), documento.ambiente(),
                    documento.serie(), documento.numero(), documento.numeradoEm(), true);
        }

        var prontidao = validacao.validar(tenantId, documentoId);
        if (!prontidao.apto()) {
            String codigos = prontidao.pendencias().stream()
                    .map(FiscalValidacaoApplicationService.Pendencia::codigo)
                    .collect(Collectors.joining(","));
            throw new IllegalArgumentException(
                    "Documento fiscal possui pendencias para numeracao: " + codigos);
        }

        FiscalPerfilFilial perfil = perfis
                .findByTenantIdAndFilialIdAndAtivoTrue(tenantId, documento.filialId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Perfil fiscal ativo nao encontrado para a filial"));
        int serie = seriePara(perfil, documento.modelo());

        var numeros = jdbc.query("""
                INSERT INTO fiscal_numeradores
                    (tenant_id, filial_id, modelo, ambiente, serie, ultimo_numero)
                VALUES (?, ?, ?, ?, ?, 1)
                ON CONFLICT (tenant_id, filial_id, modelo, ambiente, serie)
                DO UPDATE SET
                    ultimo_numero = fiscal_numeradores.ultimo_numero + 1,
                    atualizado_em = CURRENT_TIMESTAMP
                WHERE fiscal_numeradores.ultimo_numero < 999999999
                RETURNING ultimo_numero
                """, (rs, n) -> rs.getLong("ultimo_numero"),
                tenantId, documento.filialId(), documento.modelo(), documento.ambiente(), serie);
        if (numeros.isEmpty()) {
            throw new IllegalStateException("Limite da numeracao fiscal atingido para a serie");
        }

        long numero = numeros.getFirst();
        Instant numeradoEm = Instant.now();
        int atualizados = jdbc.update("""
                UPDATE fiscal_documentos
                SET serie = ?, numero = ?, numerado_em = ?
                WHERE tenant_id = ? AND id = ? AND numero IS NULL
                """, serie, numero, numeradoEm, tenantId, documentoId);
        if (atualizados != 1) {
            throw new IllegalStateException("Documento fiscal nao pode receber numeracao");
        }

        auditoria.registrar(tenantId, usuarioId, null, documento.filialId(),
                "NUMERAR_DOCUMENTO", "FISCAL_DOCUMENTO", documentoId,
                "modelo=" + documento.modelo() + ";ambiente=" + documento.ambiente()
                        + ";serie=" + serie + ";numero=" + numero);
        return new Resultado(documentoId, documento.modelo(), documento.ambiente(),
                serie, numero, numeradoEm, false);
    }

    int seriePara(FiscalPerfilFilial perfil, String modelo) {
        if ("NFE".equals(modelo)) return perfil.getSerieNfe();
        if ("NFCE".equals(modelo)) return perfil.getSerieNfce();
        throw new IllegalArgumentException("Modelo fiscal invalido para numeracao");
    }

    private record Documento(UUID id, UUID filialId, String modelo, String ambiente,
                             Integer serie, Long numero, Instant numeradoEm) {}

    public record Resultado(UUID documentoId, String modelo, String ambiente,
                            int serie, long numero, Instant numeradoEm, boolean repetida) {}
}
