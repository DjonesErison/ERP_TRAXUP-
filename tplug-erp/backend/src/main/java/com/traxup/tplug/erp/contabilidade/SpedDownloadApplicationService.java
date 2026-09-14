package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Date;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SpedDownloadApplicationService {
    private final JdbcTemplate jdbc;
    private final ObjectProvider<SpedArquivoStoragePort> storageProvider;
    private final AuditoriaApplicationService auditoria;
    private final EscopoFilialContabilidade escopoFilial;

    public SpedDownloadApplicationService(
            JdbcTemplate jdbc,
            ObjectProvider<SpedArquivoStoragePort> storageProvider,
            AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.storageProvider = storageProvider;
        this.auditoria = auditoria;
        this.escopoFilial = new EscopoFilialContabilidade(jdbc);
    }

    @Transactional
    public Download baixar(UUID tenantId, UUID usuarioId, UUID exportacaoId) {
        SpedArquivoStoragePort storage = storageProvider.getIfAvailable();
        if (storage == null)
            throw new IllegalStateException(
                    "Repositorio SPED nao configurado neste ambiente");
        var escopo = escopoFilial.resolver(tenantId, usuarioId, null);

        List<Arquivo> encontrados = jdbc.query("""
                SELECT filial_id, chave_objeto, hash_sha256, tipo, competencia
                FROM contabilidade_sped_exportacoes
                WHERE tenant_id = ?
                  AND id = ?
                  AND status = 'CONCLUIDO'
                  AND (CAST(? AS BOOLEAN) = TRUE OR filial_id IN (
                      SELECT uf.filial_id
                      FROM usuario_filiais uf
                      WHERE uf.tenant_id = ? AND uf.usuario_id = ?
                  ))
                """, (rs, n) -> new Arquivo(
                        rs.getObject("filial_id", UUID.class),
                        rs.getString("chave_objeto"),
                        rs.getString("hash_sha256"),
                        rs.getString("tipo"),
                        rs.getDate("competencia")),
                tenantId, exportacaoId,
                escopo.acessoTotal(), tenantId, usuarioId);
        if (encontrados.isEmpty())
            throw new RecursoNaoEncontradoException(
                    "Exportacao SPED concluida nao encontrada");

        Arquivo arquivo = encontrados.getFirst();
        byte[] conteudo = storage.baixar(arquivo.chave());
        if (!hashConfere(conteudo, arquivo.hashSha256()))
            throw new IllegalStateException(
                    "Integridade do arquivo SPED armazenado invalida");

        YearMonth competencia = YearMonth.from(
                arquivo.competencia().toLocalDate());
        auditoria.registrar(tenantId, usuarioId, null, arquivo.filialId(),
                "BAIXAR_SPED", "SPED_EXPORTACAO", exportacaoId,
                "tipo=" + arquivo.tipo() + ";competencia=" + competencia
                        + ";filialId=" + arquivo.filialId());
        return new Download(conteudo,
                nomeArquivo(arquivo.tipo(), competencia, exportacaoId));
    }

    static boolean hashConfere(byte[] conteudo, String esperado) {
        if (conteudo == null || esperado == null) return false;
        String atual = SpedExportacaoWorkerApplicationService.sha256(conteudo);
        return MessageDigest.isEqual(
                atual.getBytes(StandardCharsets.US_ASCII),
                esperado.getBytes(StandardCharsets.US_ASCII));
    }

    static String nomeArquivo(
            String tipo, YearMonth competencia, UUID exportacaoId) {
        String tipoSeguro = tipo.toLowerCase(Locale.ROOT)
                .replace('_', '-');
        return "sped-" + tipoSeguro + "-" + competencia
                + "-" + exportacaoId + ".txt";
    }

    record Arquivo(
            UUID filialId, String chave, String hashSha256,
            String tipo, Date competencia) {}

    public record Download(byte[] conteudo, String nomeArquivo) {}
}
