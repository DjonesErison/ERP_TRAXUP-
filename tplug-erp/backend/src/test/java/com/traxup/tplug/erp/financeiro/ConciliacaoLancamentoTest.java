package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConciliacaoLancamentoTest {
    @Test
    void deveConciliarLancamentoPendente() {
        ConciliacaoLancamento lancamento = novoLancamento();
        UUID movimentoId = UUID.randomUUID();

        lancamento.conciliar(movimentoId);

        assertEquals("CONCILIADO", lancamento.getStatus());
        assertEquals(movimentoId, lancamento.getMovimentoId());
    }

    @Test
    void naoDeveConciliarDuasVezes() {
        ConciliacaoLancamento lancamento = novoLancamento();
        lancamento.conciliar(UUID.randomUUID());

        assertThrows(RecursoConflitanteException.class, () -> lancamento.conciliar(UUID.randomUUID()));
    }

    @Test
    void naoDeveConciliarSemMovimento() {
        assertThrows(RegraNegocioException.class, () -> novoLancamento().conciliar(null));
    }

    private ConciliacaoLancamento novoLancamento() {
        return new ConciliacaoLancamento(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "OFX", "REF-001", "ENTRADA",
                new BigDecimal("100.00"), "Credito externo", Instant.now(), UUID.randomUUID());
    }
}
