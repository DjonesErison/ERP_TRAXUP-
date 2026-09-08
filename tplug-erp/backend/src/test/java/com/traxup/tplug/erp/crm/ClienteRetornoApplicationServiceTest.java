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
class ClienteRetornoApplicationServiceTest {
    @Mock PessoaRepository pessoaRepository;
    @Mock FilialRepository filialRepository;

    @Test
    void deveConsultarClientesInativosComEscopoTenantFilialELimite() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        ClienteInativoProjection cliente = org.mockito.Mockito.mock(ClienteInativoProjection.class);
        when(filialRepository.existsByIdAndTenantId(filialId, tenantId)).thenReturn(true);
        when(pessoaRepository.buscarClientesInativos(eq(tenantId), eq(filialId), any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(cliente));
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);

        var service = new ClienteRetornoApplicationService(pessoaRepository, filialRepository);
        var resultado = service.listarInativos(tenantId, filialId, 45, 25);

        assertEquals(List.of(cliente), resultado);
        verify(pessoaRepository).buscarClientesInativos(eq(tenantId), eq(filialId), any(Instant.class), pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(25, pageable.getValue().getPageSize());
    }

    @Test
    void deveAplicarPadroesQuandoParametrosForemNulos() {
        UUID tenantId = UUID.randomUUID();
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(pessoaRepository.buscarClientesInativos(eq(tenantId), eq(null), any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of());

        var service = new ClienteRetornoApplicationService(pessoaRepository, filialRepository);
        service.listarInativos(tenantId, null, null, null);

        verify(pessoaRepository).buscarClientesInativos(eq(tenantId), eq(null), any(Instant.class), pageable.capture());
        assertEquals(100, pageable.getValue().getPageSize());
        verifyNoInteractions(filialRepository);
    }

    @Test
    void deveRejeitarParametrosForaDaFaixaAntesDeConsultarRepositorios() {
        var service = new ClienteRetornoApplicationService(pessoaRepository, filialRepository);

        assertThrows(RegraNegocioException.class, () -> service.listarInativos(UUID.randomUUID(), null, 0, 100));
        assertThrows(RegraNegocioException.class, () -> service.listarInativos(UUID.randomUUID(), null, 30, 501));

        verifyNoInteractions(pessoaRepository, filialRepository);
    }

    @Test
    void deveRejeitarFilialDeOutroTenantAntesDaConsultaDeClientes() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        when(filialRepository.existsByIdAndTenantId(filialId, tenantId)).thenReturn(false);

        var service = new ClienteRetornoApplicationService(pessoaRepository, filialRepository);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.listarInativos(tenantId, filialId, 30, 100));
        verifyNoInteractions(pessoaRepository);
    }
}
