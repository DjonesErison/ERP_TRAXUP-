package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.financeiro.LivroCaixaContabilidadeApplicationService;
import com.traxup.tplug.erp.inventario.InventarioContabilidadeApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class RelatoriosContabeisExportacaoApplicationService {
    private static final long LIMITE_REGISTROS = 100_000;
    private final LivroCaixaContabilidadeApplicationService livroCaixa;
    private final InventarioContabilidadeApplicationService inventarios;
    private final AuditoriaApplicationService auditoria;

    public RelatoriosContabeisExportacaoApplicationService(
            LivroCaixaContabilidadeApplicationService livroCaixa,
            InventarioContabilidadeApplicationService inventarios,
            AuditoriaApplicationService auditoria) {
        this.livroCaixa = livroCaixa;
        this.inventarios = inventarios;
        this.auditoria = auditoria;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Resultado exportarLivroCaixa(
            UUID tenantId, UUID usuarioId, LocalDate inicio, LocalDate fim,
            UUID filialId) {
        var primeira = livroCaixa.consultar(
                tenantId, usuarioId, inicio, fim, filialId, 1000, 1);
        validarQuantidade(primeira.totalDisponivel(), "livro-caixa");
        List<LivroCaixaContabilidadeApplicationService.Lancamento> itens =
                new ArrayList<>(primeira.lancamentos());
        for (int pagina = 2; pagina <= primeira.totalPaginas(); pagina++) {
            itens.addAll(livroCaixa.consultar(
                    tenantId, usuarioId, inicio, fim, filialId, 1000, pagina)
                    .lancamentos());
        }
        byte[] conteudo = csvLivro(itens);
        return registrar(tenantId, usuarioId, filialId,
                "EXPORTAR_LIVRO_CAIXA_CSV", "LIVRO_CAIXA",
                nome("livro-caixa", inicio, fim), itens.size(), conteudo);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Resultado exportarInventarios(
            UUID tenantId, UUID usuarioId, LocalDate inicio, LocalDate fim,
            UUID filialId) {
        var primeira = inventarios.consultar(
                tenantId, usuarioId, inicio, fim, filialId, 500, 1);
        validarQuantidade(primeira.totalDisponivel(), "inventários");
        List<InventarioContabilidadeApplicationService.Posicao> itens =
                new ArrayList<>(primeira.inventarios());
        for (int pagina = 2; pagina <= primeira.totalPaginas(); pagina++) {
            itens.addAll(inventarios.consultar(
                    tenantId, usuarioId, inicio, fim, filialId, 500, pagina)
                    .inventarios());
        }
        byte[] conteudo = csvInventarios(itens);
        return registrar(tenantId, usuarioId, filialId,
                "EXPORTAR_INVENTARIOS_CSV", "INVENTARIO_SESSAO",
                nome("inventarios", inicio, fim), itens.size(), conteudo);
    }

    private Resultado registrar(
            UUID tenantId, UUID usuarioId, UUID filialId, String evento,
            String entidade, String nomeArquivo, int total, byte[] conteudo) {
        String hash = sha256(conteudo);
        auditoria.registrar(tenantId, usuarioId, null, filialId,
                evento, entidade, UUID.randomUUID(),
                "filialId=" + filialId + ";registros=" + total
                        + ";hash=" + hash);
        return new Resultado(nomeArquivo, total, hash, conteudo);
    }

    static byte[] csvLivro(
            List<LivroCaixaContabilidadeApplicationService.Lancamento> itens) {
        StringBuilder csv = new StringBuilder(
                "\uFEFFdata;filial_id;conta;tipo_conta;movimento;valor;"
                        + "descricao;origem;origem_id\n");
        for (var item : itens) {
            csv.append(campo(item.ocorridoEm())).append(';')
                    .append(campo(item.filialId())).append(';')
                    .append(campo(item.contaNome())).append(';')
                    .append(campo(item.contaTipo())).append(';')
                    .append(campo(item.tipo())).append(';')
                    .append(campo(decimal(item.valor()))).append(';')
                    .append(campo(item.descricao())).append(';')
                    .append(campo(item.origemTipo())).append(';')
                    .append(campo(item.origemId())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    static byte[] csvInventarios(
            List<InventarioContabilidadeApplicationService.Posicao> itens) {
        StringBuilder csv = new StringBuilder(
                "\uFEFFinventario_id;filial_id;descricao;concluido_em;"
                        + "ajustado_em;total_itens;itens_divergentes\n");
        for (var item : itens) {
            csv.append(campo(item.inventarioId())).append(';')
                    .append(campo(item.filialId())).append(';')
                    .append(campo(item.descricao())).append(';')
                    .append(campo(item.concluidoEm())).append(';')
                    .append(campo(item.ajustadoEm())).append(';')
                    .append(campo(item.totalItens())).append(';')
                    .append(campo(item.itensDivergentes())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    static String campo(Object valor) {
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

    private static String decimal(BigDecimal valor) {
        return valor == null ? "" : valor.toPlainString().replace('.', ',');
    }

    private static void validarQuantidade(long total, String relatorio) {
        if (total > LIMITE_REGISTROS)
            throw new IllegalArgumentException(
                    "Exportação de " + relatorio + " excede "
                            + LIMITE_REGISTROS + " registros");
    }

    private static String nome(
            String relatorio, LocalDate inicio, LocalDate fim) {
        return "traxup-" + relatorio + "-" + inicio + "-a-" + fim + ".csv";
    }

    private static String sha256(byte[] conteudo) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(conteudo));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public record Resultado(
            String nomeArquivo, int totalRegistros,
            String hashSha256, byte[] conteudo) {}
}
