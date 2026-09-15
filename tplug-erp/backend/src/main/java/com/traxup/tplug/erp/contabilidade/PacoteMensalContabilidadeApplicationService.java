package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.fiscal.FiscalExportacaoContabilidadeApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PacoteMensalContabilidadeApplicationService {
    private static final long LIMITE_BYTES = 250L * 1024 * 1024;
    private static final long LIMITE_PARTES_XML = 200;

    private final ChecklistFechamentoContabilidadeApplicationService checklist;
    private final FiscalExportacaoContabilidadeApplicationService fiscal;
    private final RelatoriosContabeisExportacaoApplicationService relatorios;
    private final SpedExportacaoApplicationService sped;
    private final SpedDownloadApplicationService downloadsSped;
    private final AuditoriaApplicationService auditoria;

    public PacoteMensalContabilidadeApplicationService(
            ChecklistFechamentoContabilidadeApplicationService checklist,
            FiscalExportacaoContabilidadeApplicationService fiscal,
            RelatoriosContabeisExportacaoApplicationService relatorios,
            SpedExportacaoApplicationService sped,
            SpedDownloadApplicationService downloadsSped,
            AuditoriaApplicationService auditoria) {
        this.checklist = checklist;
        this.fiscal = fiscal;
        this.relatorios = relatorios;
        this.sped = sped;
        this.downloadsSped = downloadsSped;
        this.auditoria = auditoria;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Resultado gerar(UUID tenantId, UUID usuarioId,
                           YearMonth competencia, UUID filialId) {
        validarSolicitacao(competencia, filialId);
        var conferencia = checklist.consultar(
                tenantId, usuarioId, competencia, filialId);
        if (!conferencia.podeGerarPacote())
            throw new IllegalArgumentException(
                    "Fechamento possui falhas ou processamentos pendentes");

        LocalDate inicio = competencia.atDay(1);
        LocalDate fim = competencia.atEndOfMonth();
        List<ArquivoPacote> arquivos = new ArrayList<>();

        byte[] checklistCsv = checklistCsv(conferencia);
        adicionar(arquivos, new ArquivoPacote(
                "checklist.csv", conferencia.itens().size(),
                sha256(checklistCsv), checklistCsv));

        var livro = relatorios.exportarLivroCaixa(
                tenantId, usuarioId, inicio, fim, filialId);
        adicionar(arquivos, new ArquivoPacote(
                "relatorios/" + livro.nomeArquivo(),
                livro.totalRegistros(), livro.hashSha256(), livro.conteudo()));

        var inventarios = relatorios.exportarInventarios(
                tenantId, usuarioId, inicio, fim, filialId);
        adicionar(arquivos, new ArquivoPacote(
                "relatorios/" + inventarios.nomeArquivo(),
                inventarios.totalRegistros(), inventarios.hashSha256(),
                inventarios.conteudo()));

        var primeiraParte = fiscal.exportar(
                tenantId, usuarioId, inicio, fim, filialId, 1);
        if (primeiraParte.totalPartes() > LIMITE_PARTES_XML)
            throw new IllegalArgumentException(
                    "Pacote excede o limite de partes XML");
        adicionarXml(arquivos, primeiraParte);
        for (int parte = 2; parte <= primeiraParte.totalPartes(); parte++) {
            adicionarXml(arquivos, fiscal.exportar(
                    tenantId, usuarioId, inicio, fim, filialId, parte));
        }

        var exportacoesSped = sped.listar(
                tenantId, usuarioId, filialId, null, "CONCLUIDO",
                competencia, competencia, 500);
        for (var exportacao : exportacoesSped) {
            var download = downloadsSped.baixar(
                    tenantId, usuarioId, exportacao.id());
            adicionar(arquivos, new ArquivoPacote(
                    "sped/" + download.nomeArquivo(), 1,
                    exportacao.hashSha256(), download.conteudo()));
        }

        byte[] conteudo = compactar(arquivos);
        String hash = sha256(conteudo);
        UUID pacoteId = UUID.randomUUID();
        auditoria.registrar(tenantId, usuarioId, null, filialId,
                "GERAR_PACOTE_CONTABIL_MENSAL", "FECHAMENTO_CONTABIL",
                pacoteId, "competencia=" + competencia
                        + ";filialId=" + filialId
                        + ";statusChecklist=" + conferencia.statusGeral()
                        + ";arquivos=" + arquivos.size()
                        + ";hash=" + hash);
        return new Resultado(
                pacoteId, competencia, filialId, conferencia.statusGeral(),
                nomeArquivo(competencia, filialId), arquivos.size(),
                hash, conteudo);
    }

    private static void adicionarXml(
            List<ArquivoPacote> arquivos,
            FiscalExportacaoContabilidadeApplicationService.Resultado parte) {
        adicionar(arquivos, new ArquivoPacote(
                "xml/" + parte.nomeArquivo(), parte.totalXml(),
                parte.hashSha256(), parte.conteudo()));
    }

    private static void adicionar(
            List<ArquivoPacote> arquivos, ArquivoPacote novo) {
        long total = novo.conteudo().length;
        for (ArquivoPacote arquivo : arquivos)
            total += arquivo.conteudo().length;
        if (total > LIMITE_BYTES)
            throw new IllegalArgumentException(
                    "Pacote contabil excede o limite de 250 MB");
        arquivos.add(novo);
    }

    static void validarSolicitacao(
            YearMonth competencia, UUID filialId) {
        if (competencia == null)
            throw new IllegalArgumentException("Competencia e obrigatoria");
        if (filialId == null)
            throw new IllegalArgumentException(
                    "Filial e obrigatoria para gerar o pacote");
    }

    static byte[] checklistCsv(
            ChecklistFechamentoContabilidadeApplicationService
                    .Resultado resultado) {
        StringBuilder csv = new StringBuilder(
                "\uFEFFcodigo;titulo;status;total;pendencias;mensagem\n");
        for (var item : resultado.itens()) {
            csv.append(campo(item.codigo())).append(';')
                    .append(campo(item.titulo())).append(';')
                    .append(campo(item.status())).append(';')
                    .append(campo(item.total())).append(';')
                    .append(campo(item.pendencias())).append(';')
                    .append(campo(item.mensagem())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    static byte[] compactar(List<ArquivoPacote> arquivos) {
        try {
            ByteArrayOutputStream saida = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(
                    saida, StandardCharsets.UTF_8)) {
                zip.putNextEntry(new ZipEntry("manifesto.csv"));
                zip.write(manifesto(arquivos));
                zip.closeEntry();
                for (ArquivoPacote arquivo : arquivos) {
                    zip.putNextEntry(new ZipEntry(arquivo.nome()));
                    zip.write(arquivo.conteudo());
                    zip.closeEntry();
                }
            }
            return saida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Falha ao compactar pacote contabil mensal", e);
        }
    }

    private static byte[] manifesto(List<ArquivoPacote> arquivos) {
        StringBuilder csv = new StringBuilder(
                "\uFEFFarquivo;registros;hash_sha256\n");
        for (ArquivoPacote arquivo : arquivos) {
            csv.append(campo(arquivo.nome())).append(';')
                    .append(campo(arquivo.registros())).append(';')
                    .append(campo(arquivo.hashSha256())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String campo(Object valor) {
        String texto = valor == null ? "" : valor.toString()
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("\"", "\"\"");
        if (!texto.isEmpty() && "=+-@".indexOf(texto.charAt(0)) >= 0)
            texto = "'" + texto;
        return "\"" + texto + "\"";
    }

    static String nomeArquivo(YearMonth competencia, UUID filialId) {
        return "traxup-pacote-contabil-" + competencia
                + "-" + filialId + ".zip";
    }

    static String sha256(byte[] conteudo) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(conteudo));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    record ArquivoPacote(
            String nome, int registros, String hashSha256, byte[] conteudo) {}

    public record Resultado(
            UUID pacoteId, YearMonth competencia, UUID filialId,
            String statusChecklist, String nomeArquivo, int totalArquivos,
            String hashSha256, byte[] conteudo) {}
}
