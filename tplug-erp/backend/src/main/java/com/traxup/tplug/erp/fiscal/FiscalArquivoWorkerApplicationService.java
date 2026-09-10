package com.traxup.tplug.erp.fiscal;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class FiscalArquivoWorkerApplicationService {
    private final JdbcTemplate jdbc;
    private final ObjectProvider<FiscalArquivoStoragePort> storageProvider;

    public FiscalArquivoWorkerApplicationService(
            JdbcTemplate jdbc,
            ObjectProvider<FiscalArquivoStoragePort> storageProvider) {
        this.jdbc = jdbc;
        this.storageProvider = storageProvider;
    }

    @Transactional
    public Resultado processar(UUID tenantId, Integer limiteSolicitado) {
        int limite = normalizarLimite(limiteSolicitado);
        FiscalArquivoStoragePort storage = storageProvider.getIfAvailable();
        if (storage == null)
            throw new IllegalStateException(
                    "Repositorio fiscal nao configurado neste ambiente");

        List<Arquivo> arquivos = jdbc.query("""
                SELECT a.id, a.chave_objeto, a.hash_sha256, p.conteudo
                FROM fiscal_arquivos a
                JOIN fiscal_documentos_processados p
                  ON p.tenant_id = a.tenant_id AND p.id = a.processado_id
                WHERE a.tenant_id = ? AND a.status IN ('PENDENTE', 'FALHOU')
                ORDER BY a.criado_em, a.id
                FOR UPDATE OF a SKIP LOCKED
                LIMIT ?
                """, (rs, n) -> new Arquivo(
                        rs.getObject("id", UUID.class),
                        rs.getString("chave_objeto"),
                        rs.getString("hash_sha256"),
                        rs.getString("conteudo")),
                tenantId, limite);

        int arquivados = 0;
        int falhas = 0;
        for (Arquivo arquivo : arquivos) {
            jdbc.update("""
                    UPDATE fiscal_arquivos
                    SET status = 'ARQUIVANDO',
                        tentativas_envio = tentativas_envio + 1
                    WHERE tenant_id = ? AND id = ?
                    """, tenantId, arquivo.id());
            try {
                if (!hashValido(arquivo.conteudo(), arquivo.hash()))
                    throw new IllegalStateException(
                            "Integridade do XML processado invalida");
                storage.armazenar(arquivo.chave(),
                        arquivo.conteudo().getBytes(StandardCharsets.UTF_8),
                        arquivo.hash());
                jdbc.update("""
                        UPDATE fiscal_arquivos
                        SET status = 'ARQUIVADO', arquivado_em = CURRENT_TIMESTAMP
                        WHERE tenant_id = ? AND id = ?
                        """, tenantId, arquivo.id());
                arquivados++;
            } catch (RuntimeException erro) {
                jdbc.update("""
                        UPDATE fiscal_arquivos
                        SET status = 'FALHOU', arquivado_em = NULL
                        WHERE tenant_id = ? AND id = ?
                        """, tenantId, arquivo.id());
                falhas++;
            }
        }
        return new Resultado(arquivos.size(), arquivados, falhas);
    }

    static int normalizarLimite(Integer limite) {
        if (limite == null) return 10;
        if (limite < 1 || limite > 20)
            throw new IllegalArgumentException(
                    "Limite do worker deve estar entre 1 e 20");
        return limite;
    }

    static boolean hashValido(String conteudo, String esperado) {
        try {
            String atual = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(conteudo.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(atual.getBytes(StandardCharsets.US_ASCII),
                    esperado.getBytes(StandardCharsets.US_ASCII));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    record Arquivo(UUID id, String chave, String hash, String conteudo) {}

    public record Resultado(int processados, int arquivados, int falhas) {}
}
