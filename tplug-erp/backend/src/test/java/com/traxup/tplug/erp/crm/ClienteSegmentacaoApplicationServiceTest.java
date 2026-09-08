package com.traxup.tplug.erp.crm;

import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteSegmentacaoApplicationServiceTest {
    @Mock PessoaRepository pessoaRepository;
    @Mock FilialRepository filialRepository;

    @Test
    void deveConsultarRfmNoTenant() {
        UUID tenantId = UUID.randomUUID();
        when(pessoaRepository.buscarMetricasRfm(any(), any(), any(), any())).thenReturn(List.of());

        novoService().listarRfm(tenantId, null, 90, 50);

        verify(pessoaRepository).buscarMetricasRfm(any(), any(), any(), any());
    }

    @Test
    void deveRejeitarLimiteInvalidoAntesDaConsulta() {
        assertThrows(RegraNegocioException.class,
                () -> novoService().listarRfm(UUID.randomUUID(), null, 365, 501));

        verify(pessoaRepository, never()).buscarMetricasRfm(any(), any(), any(), any());
    }

    @Test
    void deveRejeitarFilialDeOutroTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        when(filialRepository.existsByIdAndTenantId(filialId, tenantId)).thenReturn(false);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> novoService().listarRfm(tenantId, filialId, 365, 100));

        verify(pessoaRepository, never()).buscarMetricasRfm(any(), any(), any(), any());
    }

    private ClienteSegmentacaoApplicationService novoService() {
        return new ClienteSegmentacaoApplicationService(pessoaRepository, filialRepository);
    }
}
