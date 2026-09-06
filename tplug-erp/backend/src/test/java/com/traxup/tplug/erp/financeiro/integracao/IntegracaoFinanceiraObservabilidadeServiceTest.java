package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IntegracaoFinanceiraObservabilidadeServiceTest {
    @Test
    void deveIsolarConsultaPorTenant() {
        UUID tenant = UUID.randomUUID(); UUID integracaoId = UUID.randomUUID();
        var tentativas = mock(IntegracaoFinanceiraTentativaRepository.class);
        var integracoes = mock(IntegracaoFinanceiraRepository.class);
        when(integracoes.findByIdAndTenantId(integracaoId, tenant)).thenReturn(Optional.empty());
        var service = new IntegracaoFinanceiraObservabilidadeService(tentativas, integracoes);
        assertThrows(RecursoNaoEncontradoException.class, () -> service.listar(tenant, integracaoId));
        verifyNoInteractions(tentativas);
    }

    @Test
    void deveRegistrarFalhaSemPersistirMensagemDoErro() {
        UUID tenant = UUID.randomUUID();
        var integracao = new IntegracaoFinanceira(tenant, UUID.randomUUID(), UUID.randomUUID(), "banco-x", null, UUID.randomUUID());
        var tentativas = mock(IntegracaoFinanceiraTentativaRepository.class);
        var integracoes = mock(IntegracaoFinanceiraRepository.class);
        var service = new IntegracaoFinanceiraObservabilidadeService(tentativas, integracoes);
        var captor = org.mockito.ArgumentCaptor.forClass(IntegracaoFinanceiraTentativa.class);
        Instant agora = Instant.now();
        service.falha(integracao, 3, 10, new IllegalStateException("token=segredo"), agora, agora.plusMillis(10));
        verify(tentativas).save(captor.capture());
        assertEquals("FALHA", captor.getValue().getStatus());
        assertEquals("IllegalStateException", captor.getValue().getErroCodigo());
        assertFalse(captor.getValue().getErroCodigo().contains("segredo"));
        assertEquals(tenant, captor.getValue().getTenantId());
    }
}
