package com.traxup.tplug.erp.financeiro.ofx;

import com.traxup.tplug.erp.financeiro.ConciliacaoApplicationService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OfxExtratoParser {
    private static final Pattern TRANSACAO = Pattern.compile("(?is)<STMTTRN>(.*?)(?:</STMTTRN>|(?=<STMTTRN>|</BANKTRANLIST>))");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public List<ConciliacaoApplicationService.ImportacaoLancamento> parse(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) throw new IllegalArgumentException("Conteudo OFX e obrigatorio");
        List<ConciliacaoApplicationService.ImportacaoLancamento> itens = new ArrayList<>();
        Matcher matcher = TRANSACAO.matcher(conteudo);
        while (matcher.find()) {
            String bloco = matcher.group(1);
            String referencia = campo(bloco, "FITID", true);
            BigDecimal valorAssinado;
            try { valorAssinado = new BigDecimal(campo(bloco, "TRNAMT", true).replace(',', '.')); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Valor OFX invalido para FITID " + referencia); }
            if (valorAssinado.signum() == 0) throw new IllegalArgumentException("Valor OFX nao pode ser zero para FITID " + referencia);
            String data = campo(bloco, "DTPOSTED", true);
            Instant ocorridoEm = parseData(data, referencia);
            String descricao = campo(bloco, "MEMO", false);
            if (descricao == null || descricao.isBlank()) descricao = campo(bloco, "NAME", false);
            if (descricao == null || descricao.isBlank()) descricao = "Lancamento OFX " + referencia;
            itens.add(new ConciliacaoApplicationService.ImportacaoLancamento(
                    "OFX", referencia, valorAssinado.signum() > 0 ? "ENTRADA" : "SAIDA",
                    valorAssinado.abs(), descricao.trim(), ocorridoEm));
        }
        if (itens.isEmpty()) throw new IllegalArgumentException("OFX nao contem lancamentos STMTTRN");
        if (itens.size() > 500) throw new IllegalArgumentException("OFX excede o limite de 500 lancamentos por importacao");
        return itens;
    }

    private Instant parseData(String valor, String referencia) {
        String digitos = valor.replaceAll("[^0-9].*$", "");
        if (digitos.length() < 8) throw new IllegalArgumentException("Data OFX invalida para FITID " + referencia);
        String normalizado = digitos.substring(0, Math.min(14, digitos.length()));
        if (normalizado.length() == 8) normalizado += "000000";
        else if (normalizado.length() < 14) normalizado = String.format(Locale.ROOT, "%-14s", normalizado).replace(' ', '0');
        try { return LocalDateTime.parse(normalizado, DATA).toInstant(ZoneOffset.UTC); }
        catch (RuntimeException e) { throw new IllegalArgumentException("Data OFX invalida para FITID " + referencia); }
    }

    private String campo(String bloco, String nome, boolean obrigatorio) {
        Matcher matcher = Pattern.compile("(?is)<" + nome + ">\\s*([^<\\r\\n]+)").matcher(bloco);
        if (matcher.find()) return matcher.group(1).trim();
        if (obrigatorio) throw new IllegalArgumentException("Campo OFX obrigatorio ausente: " + nome);
        return null;
    }
}
