package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.contabilidade.api.FechamentoMensalContabilidadeController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FechamentoMensalContabilidadeApplicationServiceTest {
    @Test
    void calculaSaldoDoLivroCaixaDaCompetencia() {
        var resumo = new FechamentoMensalContabilidadeApplicationService
                .LivroCaixaResumo(10, new BigDecimal("1500.00"),
                        new BigDecimal("425.50"));

        assertEquals(new BigDecimal("1074.50"), resumo.saldo());
    }

    @Test
    void exigeCompetencia() {
        var service = new FechamentoMensalContabilidadeApplicationService(
                null, null);

        assertThrows(IllegalArgumentException.class,
                () -> service.consultar(
                        UUID.randomUUID(), UUID.randomUUID(), null, null));
    }

    @Test
    void exigePermissaoDedicadaNoEndpoint() throws NoSuchMethodException {
        PreAuthorize regra = FechamentoMensalContabilidadeController.class
                .getDeclaredMethod("consultar", YearMonth.class, UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals("hasAuthority('CONTABILIDADE_FECHAMENTO_LER')",
                regra.value());
    }
}
