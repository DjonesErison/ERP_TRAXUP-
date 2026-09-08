package com.traxup.tplug.erp.crm;

import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteRfvApplicationServiceTest {
    @Mock PessoaRepository pessoaRepository;
    @Mock FilialRepository filialRepository;

    @Test
    void deveConsultarRfvComEscopoTenantFilialPeriodoELimite() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-01-01T00:00:00Z");
        Instant fim = Instant.parse("2026-09-01T23:59:59Z");
        ClienteRfvProjection cliente = org.mockito.Mockito.mock(ClienteRfvProjection.class);
        when(filialRepository.existsByIdAndTenantId(filialId, tenantId)).thenReturn(true);
        when(pessoaRepository.buscarMetricasRfv(eq(tenantId), eq(filialId), eq(inicio), eq(fim), any(Pageable.class)))
                .thenReturn(List.of(cliente));
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);

        var service = new ClienteRfvApplicationService(pessoaRepository, filialRepository);
        var resultado = service.listar(tenantId, filialId, inicio, fim, 25);

        assertEquals(List.of(cliente), resultado);
        verify(pessoaRepository).buscarMetricasRfv(eq(tenantId), eq(filialId), eq(inicio), eq(fim), pageable.capture());
        assertEquals(25, pageable.getValue().getPageSize());
    }

    @Test
    void deveAplicarLimitePadraoSemConsultarFilialQuandoAusente() {
        UUID tenantId = UUID.randomUUID();
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(pessoaRepository.buscarMetricasRfv(eq(tenantId), eq(null), eq(null), eq(null), any(Pageable.class))).thenReturn(List.of());

        var service = new ClienteRfvApplicationService(pessoaRepository, filialRepository);
        service.listar(tenantId, null, null, null, null);

        verify(pessoaRepository).buscarMetricasRfv(eq(tenantId), eq(null), eq(null), eq(null), pageable.capture());
        assertEquals(100, pageable.getValue().getPageSize());
        verifyNoInteractions(filialRepository);
    }

    @Test
    void deveRejeitarPeriodoELimiteInvalidosAntesDosRepositorios() {
        var service = new ClienteRfvApplicationService(pessoaRepository, filialRepository);
        Instant agora = Instant.now();

        assertThrows(RegraNegocioException.class, () -> service.listar(UUID.randomUUID(), null, agora, agora.minusSeconds(1), 100));
        assertThrows(RegraNegocioException.class, () -> service.listar(UUID.randomUUID(), null, null, null, 501));
        verifyNoInteractions(pessoaRepository, filialRepository);
    }

    @Test
    void deveRejeitarFilialDeOutroTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        when(filialRepository.existsByIdAndTenantId(filialId, tenantId)).thenReturn(false);
        var service = new ClienteRfvApplicationService(pessoaRepository, filialRepository);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.listar(tenantId, filialId, null, null, 100));
        verifyNoInteractions(pessoaRepository);
    }
}
