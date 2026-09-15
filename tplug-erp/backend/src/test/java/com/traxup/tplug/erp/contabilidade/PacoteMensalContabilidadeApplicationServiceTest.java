package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.contabilidade.api.PacoteMensalContabilidadeController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacoteMensalContabilidadeApplicationServiceTest {
    @Test
    void compactaArquivosComManifestoEIntegridade() throws Exception {
        byte[] conteudo = "conteudo".getBytes(StandardCharsets.UTF_8);
        var arquivo = new PacoteMensalContabilidadeApplicationService
                .ArquivoPacote("relatorios/teste.csv", 2,
                        PacoteMensalContabilidadeApplicationService
                                .sha256(conteudo), conteudo);

        byte[] zip = PacoteMensalContabilidadeApplicationService
                .compactar(List.of(arquivo));
        List<String> nomes = new ArrayList<>();
        String manifesto;
        try (ZipInputStream entrada = new ZipInputStream(
                new ByteArrayInputStream(zip), StandardCharsets.UTF_8)) {
            var primeiro = entrada.getNextEntry();
            nomes.add(primeiro.getName());
            manifesto = new String(entrada.readAllBytes(),
                    StandardCharsets.UTF_8);
            nomes.add(entrada.getNextEntry().getName());
            assertEquals("conteudo", new String(
                    entrada.readAllBytes(), StandardCharsets.UTF_8));
        }

        assertEquals(List.of("manifesto.csv", "relatorios/teste.csv"), nomes);
        assertTrue(manifesto.contains("\"relatorios/teste.csv\";\"2\""));
        assertTrue(manifesto.contains(arquivo.hashSha256()));
    }

    @Test
    void geraChecklistCsvComStatusEPendencias() {
        var item = new ChecklistFechamentoContabilidadeApplicationService.Item(
                "XML", "Documentos XML", "ATENCAO", 4, 1,
                "Revisar arquivo");
        var resultado =
                new ChecklistFechamentoContabilidadeApplicationService.Resultado(
                        YearMonth.of(2026, 9), UUID.randomUUID(),
                        "ATENCAO", true, 1, List.of(item));

        String csv = new String(
                PacoteMensalContabilidadeApplicationService
                        .checklistCsv(resultado),
                StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("\uFEFFcodigo;"));
        assertTrue(csv.contains("\"XML\";\"Documentos XML\";\"ATENCAO\""));
        assertTrue(csv.contains(";\"4\";\"1\";\"Revisar arquivo\""));
    }

    @Test
    void exigeCompetenciaEFilial() {
        assertThrows(IllegalArgumentException.class,
                () -> PacoteMensalContabilidadeApplicationService
                        .validarSolicitacao(null, UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class,
                () -> PacoteMensalContabilidadeApplicationService
                        .validarSolicitacao(YearMonth.of(2026, 9), null));
    }

    @Test
    void exigeTodasAsPermissoesDosConteudosDoPacote() throws Exception {
        PreAuthorize regra = PacoteMensalContabilidadeController.class
                .getDeclaredMethod(
                        "gerar", YearMonth.class, UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertTrue(regra.value().contains(
                "CONTABILIDADE_FECHAMENTO_LER"));
        assertTrue(regra.value().contains(
                "FISCAL_REPOSITORIO_CONTABILIDADE_LER"));
        assertTrue(regra.value().contains(
                "CONTABILIDADE_LIVRO_CAIXA_LER"));
        assertTrue(regra.value().contains(
                "CONTABILIDADE_INVENTARIO_LER"));
        assertTrue(regra.value().contains(
                "CONTABILIDADE_SPED_BAIXAR"));
    }
}
