package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FiscalTentativasHistoricoApplicationServiceTest {
    @Test
    void naoConsultaTentativasDeDocumentoAusenteNoTenant() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), any(Class.class), any(), any()))
                .thenReturn(false);
        var service = new FiscalTentativasHistoricoApplicationService(jdbc);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.buscar(UUID.randomUUID(), UUID.randomUUID()));
    }
}
