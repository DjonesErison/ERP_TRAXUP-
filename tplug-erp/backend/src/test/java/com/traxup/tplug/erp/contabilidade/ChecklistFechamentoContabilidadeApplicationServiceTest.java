package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.contabilidade.api.ChecklistFechamentoContabilidadeController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChecklistFechamentoContabilidadeApplicationServiceTest {
    @Test
    void bloqueiaFechamentoComFalhaEContaPendencias() {
        var resultado = ChecklistFechamentoContabilidadeApplicationService
                .montar(resumo(
                        new FechamentoMensalContabilidadeApplicationService
                                .XmlResumo(10, 8, 0, 2),
                        new FechamentoMensalContabilidadeApplicationService
                                .SpedResumo(1, 1, 0, 0, 0),
                        livro(5),
                        new FechamentoMensalContabilidadeApplicationService
                                .InventarioResumo(1, 1, 0)));

        assertEquals("BLOQUEADO", resultado.statusGeral());
        assertEquals(2, resultado.totalPendencias());
        assertFalse(resultado.podeGerarPacote());
    }

    @Test
    void permitePacoteComAlertasMasSemFalhasOuProcessamento() {
        var resultado = ChecklistFechamentoContabilidadeApplicationService
                .montar(resumo(
                        new FechamentoMensalContabilidadeApplicationService
                                .XmlResumo(0, 0, 0, 0),
                        new FechamentoMensalContabilidadeApplicationService
                                .SpedResumo(0, 0, 0, 0, 0),
                        livro(0),
                        new FechamentoMensalContabilidadeApplicationService
                                .InventarioResumo(1, 0, 1)));

        assertEquals("ATENCAO", resultado.statusGeral());
        assertEquals(1, resultado.totalPendencias());
        assertTrue(resultado.podeGerarPacote());
    }

    @Test
    void marcaCompetenciaProntaQuandoTodosOsItensEstaoConferidos() {
        var resultado = ChecklistFechamentoContabilidadeApplicationService
                .montar(resumo(
                        new FechamentoMensalContabilidadeApplicationService
                                .XmlResumo(4, 4, 0, 0),
                        new FechamentoMensalContabilidadeApplicationService
                                .SpedResumo(2, 2, 0, 0, 0),
                        livro(12),
                        new FechamentoMensalContabilidadeApplicationService
                                .InventarioResumo(2, 2, 0)));

        assertEquals("PRONTO", resultado.statusGeral());
        assertEquals(0, resultado.totalPendencias());
        assertTrue(resultado.podeGerarPacote());
    }

    @Test
    void protegeEndpointComPermissaoDoFechamento() throws Exception {
        PreAuthorize regra = ChecklistFechamentoContabilidadeController.class
                .getDeclaredMethod(
                        "consultar", YearMonth.class, UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals("hasAuthority('CONTABILIDADE_FECHAMENTO_LER')",
                regra.value());
    }

    private static FechamentoMensalContabilidadeApplicationService.Resumo resumo(
            FechamentoMensalContabilidadeApplicationService.XmlResumo xml,
            FechamentoMensalContabilidadeApplicationService.SpedResumo sped,
            FechamentoMensalContabilidadeApplicationService.LivroCaixaResumo livro,
            FechamentoMensalContabilidadeApplicationService
                    .InventarioResumo inventario) {
        return new FechamentoMensalContabilidadeApplicationService.Resumo(
                YearMonth.of(2026, 9), UUID.randomUUID(),
                xml, sped, livro, inventario);
    }

    private static FechamentoMensalContabilidadeApplicationService
            .LivroCaixaResumo livro(long lancamentos) {
        return new FechamentoMensalContabilidadeApplicationService
                .LivroCaixaResumo(lancamentos, BigDecimal.TEN, BigDecimal.ONE);
    }
}
