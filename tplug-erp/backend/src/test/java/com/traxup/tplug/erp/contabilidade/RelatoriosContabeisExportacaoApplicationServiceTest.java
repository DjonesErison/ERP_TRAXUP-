package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.contabilidade.api.RelatoriosContabeisExportacaoController;
import com.traxup.tplug.erp.financeiro.LivroCaixaContabilidadeApplicationService;
import com.traxup.tplug.erp.inventario.InventarioContabilidadeApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelatoriosContabeisExportacaoApplicationServiceTest {
    @Test
    void geraLivroCaixaCompativelComExcelEProtegeFormula() {
        var item = new LivroCaixaContabilidadeApplicationService.Lancamento(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Caixa principal", "CAIXA", "ENTRADA",
                new BigDecimal("1250.75"), "=HIPERLINK(...)",
                "VENDA", UUID.randomUUID(),
                Instant.parse("2026-09-14T12:00:00Z"));

        String csv = new String(
                RelatoriosContabeisExportacaoApplicationService
                        .csvLivro(List.of(item)),
                StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("\uFEFFdata;"));
        assertTrue(csv.contains("\"1250,75\""));
        assertTrue(csv.contains("\"'=HIPERLINK(...)\""));
    }

    @Test
    void geraInventariosComPosicaoEDivergencias() {
        var item = new InventarioContabilidadeApplicationService.Posicao(
                UUID.randomUUID(), UUID.randomUUID(), "Contagem mensal",
                Instant.parse("2026-09-14T12:00:00Z"), null, 120, 3);

        String csv = new String(
                RelatoriosContabeisExportacaoApplicationService
                        .csvInventarios(List.of(item)),
                StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("\uFEFFinventario_id;"));
        assertTrue(csv.contains("\"Contagem mensal\""));
        assertTrue(csv.contains(";\"120\";\"3\""));
    }

    @Test
    void protegeEndpointsComPermissoesDedicadas() throws Exception {
        PreAuthorize livro = RelatoriosContabeisExportacaoController.class
                .getDeclaredMethod("exportarLivroCaixa",
                        LocalDate.class, LocalDate.class, UUID.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize inventario = RelatoriosContabeisExportacaoController.class
                .getDeclaredMethod("exportarInventarios",
                        LocalDate.class, LocalDate.class, UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals("hasAuthority('CONTABILIDADE_LIVRO_CAIXA_LER')",
                livro.value());
        assertEquals("hasAuthority('CONTABILIDADE_INVENTARIO_LER')",
                inventario.value());
    }
}
