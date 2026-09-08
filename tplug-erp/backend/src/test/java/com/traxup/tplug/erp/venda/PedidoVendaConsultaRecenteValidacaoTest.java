package com.traxup.tplug.erp.venda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PedidoVendaConsultaRecenteValidacaoTest {
    @Mock PedidoVendaRepository repository;

    private PedidoVendaConsultaRecenteService service;

    @BeforeEach
    void setUp() {
        service = new PedidoVendaConsultaRecenteService(repository);
    }

    @Test
    void deveRejeitarLimiteForaDaFaixaSemConsultarRepositorio() {
        UUID tenantId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> service.listar(tenantId, 0, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.listar(tenantId, 101, null, null, null, null, null));

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarStatusInvalidoSemConsultarRepositorio() {
        assertThrows(IllegalArgumentException.class,
                () -> service.listar(UUID.randomUUID(), 20, null, null, "DESCONHECIDO", null, null));

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarInicioPosteriorAoFimSemConsultarRepositorio() {
        Instant inicio = Instant.parse("2026-09-08T13:00:00Z");
        Instant fim = Instant.parse("2026-09-08T12:00:00Z");

        assertThrows(IllegalArgumentException.class,
                () -> service.listar(UUID.randomUUID(), 20, null, null, null, inicio, fim));

        verifyNoInteractions(repository);
    }
}
