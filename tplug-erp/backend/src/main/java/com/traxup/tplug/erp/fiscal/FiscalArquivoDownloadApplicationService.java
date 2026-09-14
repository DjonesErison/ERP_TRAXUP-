package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.contabilidade.EscopoFilialContabilidade;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class FiscalArquivoDownloadApplicationService {
    private final JdbcTemplate jdbc;
    private final ObjectProvider<FiscalArquivoStoragePort> storageProvider;
    private final AuditoriaApplicationService auditoria;
    private final EscopoFilialContabilidade escopoFilial;

    public FiscalArquivoDownloadApplicationService(
            JdbcTemplate jdbc,
            ObjectProvider<FiscalArquivoStoragePort> storageProvider,
            AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.storageProvider = storageProvider;
        this.auditoria = auditoria;
        this.escopoFilial = new EscopoFilialContabilidade(jdbc);
    }

    @Transactional
    public Resultado baixar(UUID tenantId, UUID usuarioId, UUID arquivoId) {
        var escopo = escopoFilial.resolver(tenantId, usuarioId, null);
        Origem origem = jdbc.query("""
                SELECT a.id, a.documento_id, a.chave_objeto, a.hash_sha256,
                       a.status, d.filial_id
                FROM fiscal_arquivos a
                JOIN fiscal_documentos d
                  ON d.tenant_id = a.tenant_id AND d.id = a.documento_id
                WHERE a.tenant_id = ? AND a.id = ?
                  AND (CAST(? AS BOOLEAN) = TRUE OR d.filial_id IN (
                      SELECT uf.filial_id
                      FROM usuario_filiais uf
                      WHERE uf.tenant_id = ? AND uf.usuario_id = ?
                  ))
                """, (rs, n) -> new Origem(
                        rs.getObject("id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getString("chave_objeto"),
                        rs.getString("hash_sha256"), rs.getString("status"),
                        rs.getObject("filial_id", UUID.class)),
                tenantId, arquivoId,
                escopo.acessoTotal(), tenantId, usuarioId)
                .stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Arquivo fiscal nao encontrado para o tenant"));

        if (!"ARQUIVADO".equals(origem.status()))
            throw new IllegalArgumentException(
                    "Download exige arquivo fiscal ARQUIVADO");

        FiscalArquivoStoragePort storage = storageProvider.getIfAvailable();
        if (storage == null)
            throw new IllegalStateException(
                    "Repositorio fiscal nao configurado neste ambiente");

        byte[] conteudo = storage.baixar(origem.chave());
        if (!hashValido(conteudo, origem.hash()))
            throw new IllegalStateException(
                    "Integridade do arquivo fiscal armazenado invalida");

        auditoria.registrar(tenantId, usuarioId, null, origem.filialId(),
                "BAIXAR_ARQUIVO_FISCAL", "FISCAL_ARQUIVO", arquivoId,
                "documentoId=" + origem.documentoId() + ";hash=" + origem.hash()
                        + ";tamanho=" + conteudo.length);
        return new Resultado(arquivoId, origem.documentoId(),
                nomeArquivo(origem.documentoId()), origem.hash(), conteudo);
    }

    static boolean hashValido(byte[] conteudo, String esperado) {
        try {
            String atual = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(conteudo));
            return MessageDigest.isEqual(
                    atual.getBytes(StandardCharsets.US_ASCII),
                    esperado.getBytes(StandardCharsets.US_ASCII));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    static String nomeArquivo(UUID documentoId) {
        return "traxup-fiscal-" + documentoId + ".xml";
    }

    record Origem(UUID arquivoId, UUID documentoId, String chave,
                  String hash, String status, UUID filialId) {}

    public record Resultado(UUID arquivoId, UUID documentoId, String nomeArquivo,
                            String hashSha256, byte[] conteudo) {}
}
