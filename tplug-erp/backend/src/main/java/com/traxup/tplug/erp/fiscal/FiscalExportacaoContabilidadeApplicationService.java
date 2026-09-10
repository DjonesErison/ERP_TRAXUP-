package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FiscalExportacaoContabilidadeApplicationService {
    private static final int LIMITE_ARQUIVOS = 500;
    private static final String CABECALHO_MANIFESTO =
            "arquivo_id;documento_id;modelo;serie;numero;hash_sha256;nome_arquivo\n";
    private final JdbcTemplate jdbc;
    private final ObjectProvider<FiscalArquivoStoragePort> storageProvider;
    private final AuditoriaApplicationService auditoria;

    public FiscalExportacaoContabilidadeApplicationService(
            JdbcTemplate jdbc,
            ObjectProvider<FiscalArquivoStoragePort> storageProvider,
            AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.storageProvider = storageProvider;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado exportar(UUID tenantId, UUID usuarioId,
                              LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        FiscalArquivoStoragePort storage = storageProvider.getIfAvailable();
        if (storage == null)
            throw new IllegalStateException(
                    "Repositorio fiscal nao configurado neste ambiente");

        List<Arquivo> arquivos = jdbc.query("""
                SELECT a.id, a.documento_id, a.chave_objeto, a.hash_sha256,
                       d.modelo, d.serie, d.numero
                FROM fiscal_arquivos a
                JOIN fiscal_documentos d
                  ON d.tenant_id = a.tenant_id AND d.id = a.documento_id
                WHERE a.tenant_id = ? AND a.status = 'ARQUIVADO'
                  AND a.arquivado_em >= ?
                  AND a.arquivado_em < ?
                ORDER BY a.arquivado_em, a.id
                LIMIT 501
                """, (rs, n) -> new Arquivo(
                        rs.getObject("id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getString("chave_objeto"),
                        rs.getString("hash_sha256"), rs.getString("modelo"),
                        rs.getObject("serie", Integer.class),
                        rs.getObject("numero", Long.class)),
                tenantId, Timestamp.valueOf(inicio.atStartOfDay()),
                Timestamp.valueOf(fim.plusDays(1).atStartOfDay()));
        if (arquivos.size() > LIMITE_ARQUIVOS)
            throw new IllegalArgumentException(
                    "Periodo possui mais de 500 XMLs; reduza o intervalo");

        byte[] zip = compactar(storage, arquivos);
        UUID exportacaoId = UUID.randomUUID();
        String hash = sha256(zip);
        auditoria.registrar(tenantId, usuarioId, null, null,
                "EXPORTAR_XML_CONTABILIDADE", "FISCAL_EXPORTACAO",
                exportacaoId, "inicio=" + inicio + ";fim=" + fim
                        + ";arquivos=" + arquivos.size() + ";hash=" + hash);
        return new Resultado(exportacaoId, inicio, fim,
                nomeZip(inicio, fim), arquivos.size(), hash, zip);
    }

    private byte[] compactar(FiscalArquivoStoragePort storage,
                             List<Arquivo> arquivos) {
        try {
            ByteArrayOutputStream saida = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(saida,
                    StandardCharsets.UTF_8)) {
                zip.putNextEntry(new ZipEntry("manifesto.csv"));
                zip.write(manifesto(arquivos).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();

                for (Arquivo arquivo : arquivos) {
                    byte[] xml = storage.baixar(arquivo.chave());
                    if (!FiscalArquivoDownloadApplicationService
                            .hashValido(xml, arquivo.hash()))
                        throw new IllegalStateException(
                                "Integridade de XML arquivado invalida");
                    zip.putNextEntry(new ZipEntry(nomeEntrada(arquivo)));
                    zip.write(xml);
                    zip.closeEntry();
                }
            }
            return saida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Falha ao gerar pacote fiscal da contabilidade", e);
        }
    }

    static String manifesto(List<Arquivo> arquivos) {
        StringBuilder conteudo = new StringBuilder(CABECALHO_MANIFESTO);
        for (Arquivo arquivo : arquivos) {
            conteudo.append(campoCsv(arquivo.id())).append(';')
                    .append(campoCsv(arquivo.documentoId())).append(';')
                    .append(campoCsv(modeloSeguro(arquivo.modelo()))).append(';')
                    .append(campoCsv(arquivo.serie())).append(';')
                    .append(campoCsv(arquivo.numero())).append(';')
                    .append(campoCsv(arquivo.hash())).append(';')
                    .append(campoCsv(nomeEntrada(arquivo))).append('\n');
        }
        return conteudo.toString();
    }

    private static String campoCsv(Object valor) {
        if (valor == null)
            return "";
        String texto = valor.toString()
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("\"", "\"\"");
        if (!texto.isEmpty() && "=+-@".indexOf(texto.charAt(0)) >= 0)
            texto = "'" + texto;
        return "\"" + texto + "\"";
    }

    static void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null)
            throw new IllegalArgumentException(
                    "Data inicial e final sao obrigatorias");
        if (inicio.isAfter(fim))
            throw new IllegalArgumentException(
                    "Data inicial deve ser anterior ou igual a data final");
        if (ChronoUnit.DAYS.between(inicio, fim) > 31)
            throw new IllegalArgumentException(
                    "Exportacao permite no maximo 32 dias consecutivos");
    }

    private static String modeloSeguro(String modelo) {
        return modelo == null ? "FISCAL"
                : modelo.toUpperCase(Locale.ROOT)
                    .replaceAll("[^A-Z0-9_-]", "_");
    }

    static String nomeEntrada(Arquivo arquivo) {
        String serie = arquivo.serie() == null ? "SEM-SERIE"
                : arquivo.serie().toString();
        String numero = arquivo.numero() == null ? "SEM-NUMERO"
                : arquivo.numero().toString();
        return modeloSeguro(arquivo.modelo()) + "-" + serie + "-" + numero + "-"
                + arquivo.id() + ".xml";
    }

    static String nomeZip(LocalDate inicio, LocalDate fim) {
        return "traxup-xml-" + inicio + "-a-" + fim + ".zip";
    }

    private static String sha256(byte[] conteudo) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(conteudo));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    record Arquivo(UUID id, UUID documentoId, String chave, String hash,
                   String modelo, Integer serie, Long numero) {}

    public record Resultado(UUID exportacaoId, LocalDate inicio, LocalDate fim,
                            String nomeArquivo, int totalXml,
                            String hashSha256, byte[] conteudo) {}
}
