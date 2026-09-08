package com.traxup.tplug.erp.venda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoVendaConsultaRecenteValidacaoTest {
    @Mock PedidoVendaRepository repository;

    private PedidoVendaConsultaRecenteService service;

    @BeforeEach
    void setUp() {
        service = new PedidoVendaConsultaRecenteService(repository);
    }

    @Test
    void deveRejeitarPaginaNegativaSemConsultarRepositorio() {
        assertThrows(IllegalArgumentException.class,
                () -> service.listar(UUID.randomUUID(), -1, 20, null, null, null, null, null));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarTamanhoForaDaFaixaSemConsultarRepositorio() {
        UUID tenantId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,
                () -> service.listar(tenantId, 0, 0, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.listar(tenantId, 0, 101, null, null, null, null, null));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarStatusInvalidoSemConsultarRepositorio() {
        assertThrows(IllegalArgumentException.class,
                () -> service.listar(UUID.randomUUID(), 0, 20, null, null, "DESCONHECIDO", null, null));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarInicioPosteriorAoFimSemConsultarRepositorio() {
        Instant inicio = Instant.parse("2026-09-08T13:00:00Z");
        Instant fim = Instant.parse("2026-09-08T12:00:00Z");
        assertThrows(IllegalArgumentException.class,
                () -> service.listar(UUID.randomUUID(), 0, 20, null, null, null, inicio, fim));
        verifyNoInteractions(repository);
    }

    @Test
    void deveNormalizarStatusEEncaminharPaginaFiltrosETamanho() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-09-08T10:00:00Z");
        Instant fim = Instant.parse("2026-09-08T12:00:00Z");
        PageRequest pagina = PageRequest.of(2, 37);
        when(repository.buscarRecentesFiltrados(
                tenantId, filialId, clienteId, "FATURADO", inicio, fim, pagina))
                .thenReturn(new PageImpl<>(List.of(), pagina, 0));

        var resultado = service.listar(tenantId, 2, 37, filialId, clienteId, "  faturado  ", inicio, fim);

        assertEquals(2, resultado.getNumber());
        assertEquals(37, resultado.getSize());
        verify(repository).buscarRecentesFiltrados(
                tenantId, filialId, clienteId, "FATURADO", inicio, fim, pagina);
    }
}
