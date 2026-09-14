package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class ChecklistFechamentoContabilidadeApplicationService {
    private final FechamentoMensalContabilidadeApplicationService fechamento;
    private final AuditoriaApplicationService auditoria;

    public ChecklistFechamentoContabilidadeApplicationService(
            FechamentoMensalContabilidadeApplicationService fechamento,
            AuditoriaApplicationService auditoria) {
        this.fechamento = fechamento;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado consultar(
            UUID tenantId, UUID usuarioId,
            YearMonth competencia, UUID filialId) {
        var resumo = fechamento.consultar(
                tenantId, usuarioId, competencia, filialId);
        Resultado resultado = montar(resumo);
        auditoria.registrar(tenantId, usuarioId, null, filialId,
                "CONSULTAR_CHECKLIST_FECHAMENTO", "FECHAMENTO_CONTABIL",
                UUID.randomUUID(), "competencia=" + competencia
                        + ";filialId=" + filialId
                        + ";status=" + resultado.statusGeral()
                        + ";podeGerarPacote="
                        + resultado.podeGerarPacote());
        return resultado;
    }

    static Resultado montar(
            FechamentoMensalContabilidadeApplicationService.Resumo resumo) {
        Item xml = itemXml(resumo.xml());
        Item sped = itemSped(resumo.sped());
        Item livro = itemLivroCaixa(resumo.livroCaixa());
        Item inventario = itemInventario(resumo.inventario());
        List<Item> itens = List.of(xml, sped, livro, inventario);

        String statusGeral;
        if (itens.stream().anyMatch(item -> "FALHA".equals(item.status())))
            statusGeral = "BLOQUEADO";
        else if (itens.stream().anyMatch(item -> "PENDENTE".equals(item.status())))
            statusGeral = "PENDENTE";
        else if (itens.stream().anyMatch(item -> "ATENCAO".equals(item.status())))
            statusGeral = "ATENCAO";
        else
            statusGeral = "PRONTO";

        boolean podeGerarPacote = itens.stream().noneMatch(
                item -> "FALHA".equals(item.status())
                        || "PENDENTE".equals(item.status()));
        long totalPendencias = itens.stream()
                .mapToLong(Item::pendencias).sum();
        return new Resultado(
                resumo.competencia(), resumo.filialId(),
                statusGeral, podeGerarPacote, totalPendencias, itens);
    }

    private static Item itemXml(
            FechamentoMensalContabilidadeApplicationService.XmlResumo xml) {
        if (xml.falhas() > 0)
            return new Item("XML", "Documentos XML", "FALHA",
                    xml.total(), xml.falhas(),
                    xml.falhas() + " arquivo(s) com falha de arquivamento.");
        if (xml.pendentes() > 0)
            return new Item("XML", "Documentos XML", "PENDENTE",
                    xml.total(), xml.pendentes(),
                    xml.pendentes() + " arquivo(s) aguardando arquivamento.");
        return new Item("XML", "Documentos XML", "PRONTO",
                xml.total(), 0,
                xml.arquivados() + " arquivo(s) disponíveis.");
    }

    private static Item itemSped(
            FechamentoMensalContabilidadeApplicationService.SpedResumo sped) {
        if (sped.falhas() > 0)
            return new Item("SPED", "Arquivos SPED", "FALHA",
                    sped.total(), sped.falhas(),
                    sped.falhas() + " exportação(ões) com falha.");
        if (sped.pendentes() > 0)
            return new Item("SPED", "Arquivos SPED", "PENDENTE",
                    sped.total(), sped.pendentes(),
                    sped.pendentes() + " exportação(ões) em processamento.");
        if (sped.total() == 0)
            return new Item("SPED", "Arquivos SPED", "ATENCAO",
                    0, 0, "Nenhum SPED foi solicitado para a competência.");
        return new Item("SPED", "Arquivos SPED", "PRONTO",
                sped.total(), 0,
                sped.concluidos() + " exportação(ões) concluída(s).");
    }

    private static Item itemLivroCaixa(
            FechamentoMensalContabilidadeApplicationService
                    .LivroCaixaResumo livro) {
        if (livro.lancamentos() == 0)
            return new Item("LIVRO_CAIXA", "Livro-caixa", "ATENCAO",
                    0, 0, "Competência sem lançamentos financeiros.");
        return new Item("LIVRO_CAIXA", "Livro-caixa", "PRONTO",
                livro.lancamentos(), 0,
                livro.lancamentos() + " lançamento(ões) disponível(is).");
    }

    private static Item itemInventario(
            FechamentoMensalContabilidadeApplicationService
                    .InventarioResumo inventario) {
        if (inventario.comDivergencias() > 0)
            return new Item("INVENTARIO", "Inventários", "ATENCAO",
                    inventario.concluidos(), inventario.comDivergencias(),
                    inventario.comDivergencias()
                            + " inventário(s) com divergências.");
        if (inventario.concluidos() == 0)
            return new Item("INVENTARIO", "Inventários", "ATENCAO",
                    0, 0, "Nenhum inventário concluído na competência.");
        long semAjuste = Math.max(
                0, inventario.concluidos() - inventario.ajustados());
        if (semAjuste > 0)
            return new Item("INVENTARIO", "Inventários", "ATENCAO",
                    inventario.concluidos(), semAjuste,
                    semAjuste + " inventário(s) sem ajuste confirmado.");
        return new Item("INVENTARIO", "Inventários", "PRONTO",
                inventario.concluidos(), 0,
                inventario.concluidos() + " inventário(s) conferido(s).");
    }

    public record Item(
            String codigo, String titulo, String status,
            long total, long pendencias, String mensagem) {}

    public record Resultado(
            YearMonth competencia,
            UUID filialId,
            String statusGeral,
            boolean podeGerarPacote,
            long totalPendencias,
            List<Item> itens) {}
}
