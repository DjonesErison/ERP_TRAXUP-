package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.financeiro.api.LivroCaixaContabilidadeController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LivroCaixaContabilidadeApplicationServiceTest {
    @Test
    void consolidaEntradasSaidasESaldoDoPeriodo() {
        List<LivroCaixaContabilidadeApplicationService.Lancamento> lancamentos =
                List.of(lancamento("ENTRADA", "100.00"),
                        lancamento("SAIDA", "40.50"));

        var totais = LivroCaixaContabilidadeApplicationService
                .calcularTotais(lancamentos);

        assertEquals(new BigDecimal("100.00"), totais.entradas());
        assertEquals(new BigDecimal("40.50"), totais.saidas());
        assertEquals(new BigDecimal("59.50"), totais.saldo());
    }

    @Test
    void mantemSaldoConsolidadoIndependenteDaPagina() {
        var resumo = new LivroCaixaContabilidadeApplicationService.ResumoPeriodo(
                1501, new BigDecimal("9876.50"),
                new BigDecimal("4321.25"));

        assertEquals(1501, resumo.total());
        assertEquals(new BigDecimal("5555.25"), resumo.saldo());
        assertEquals(4, LivroCaixaContabilidadeApplicationService
                .totalPaginas(resumo.total(), 500));
    }

    @Test
    void limitaPeriodoQuantidadeEPaginaDaConsulta() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);

        LivroCaixaContabilidadeApplicationService
                .validarPeriodo(inicio, inicio.plusDays(365));
        assertEquals(500, LivroCaixaContabilidadeApplicationService
                .validarLimite(null));
        assertEquals(1, LivroCaixaContabilidadeApplicationService
                .totalPaginas(0, 500));
        LivroCaixaContabilidadeApplicationService.validarPagina(2, 3);

        assertThrows(IllegalArgumentException.class,
                () -> LivroCaixaContabilidadeApplicationService
                        .validarPeriodo(inicio, inicio.plusDays(366)));
        assertThrows(IllegalArgumentException.class,
                () -> LivroCaixaContabilidadeApplicationService
                        .validarLimite(1001));
        assertThrows(IllegalArgumentException.class,
                () -> LivroCaixaContabilidadeApplicationService
                        .validarPagina(0, 3));
        assertThrows(IllegalArgumentException.class,
                () -> LivroCaixaContabilidadeApplicationService
                        .validarPagina(4, 3));
    }

    @Test
    void exigePermissaoDedicadaNoEndpoint() throws NoSuchMethodException {
        PreAuthorize regra = LivroCaixaContabilidadeController.class
                .getDeclaredMethod("consultar", LocalDate.class,
                        LocalDate.class, UUID.class, Integer.class, int.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals(
                "hasAuthority('CONTABILIDADE_LIVRO_CAIXA_LER')",
                regra.value());
    }

    private LivroCaixaContabilidadeApplicationService.Lancamento lancamento(
            String tipo, String valor) {
        return new LivroCaixaContabilidadeApplicationService.Lancamento(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Caixa principal", "CAIXA", tipo, new BigDecimal(valor),
                "Movimento contabil", null, null, Instant.now());
    }
}
