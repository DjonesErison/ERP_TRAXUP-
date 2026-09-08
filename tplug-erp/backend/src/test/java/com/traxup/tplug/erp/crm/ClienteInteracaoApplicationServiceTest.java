package com.traxup.tplug.erp.crm;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.Pessoa;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteInteracaoApplicationServiceTest {
    @Mock ClienteInteracaoRepository repository;
    @Mock ClienteFollowUpRepository followUpRepository;
    @Mock PessoaRepository pessoaRepository;
    @Mock FilialRepository filialRepository;
    @Mock AuditoriaApplicationService auditoria;

    private ClienteInteracaoApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ClienteInteracaoApplicationService(repository, followUpRepository, pessoaRepository, filialRepository, auditoria);
    }

    @Test
    void deveListarComEscopoTenantFiltrosELimite() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        Pessoa cliente = mock(Pessoa.class);
        when(cliente.isCliente()).thenReturn(true);
        when(filialRepository.existsByIdAndTenantId(filialId, tenantId)).thenReturn(true);
        when(pessoaRepository.findByIdAndTenantId(clienteId, tenantId)).thenReturn(Optional.of(cliente));
        when(repository.buscar(eq(tenantId), eq(filialId), eq(clienteId), eq("EMAIL"), eq("INTERESSE"),
                eq(null), eq(null), any(Pageable.class))).thenReturn(List.of());
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);

        service.listar(tenantId, filialId, clienteId, "email", "interesse", null, null, 25);

        verify(repository).buscar(eq(tenantId), eq(filialId), eq(clienteId), eq("EMAIL"), eq("INTERESSE"),
                eq(null), eq(null), pageable.capture());
        assertEquals(25, pageable.getValue().getPageSize());
    }

    @Test
    void deveRejeitarPeriodoInvalidoAntesDosRepositorios() {
        Instant agora = Instant.now();
        assertThrows(RegraNegocioException.class,
                () -> service.listar(UUID.randomUUID(), null, null, null, null, agora, agora.minusSeconds(1), 100));
        verifyNoInteractions(repository, followUpRepository, pessoaRepository, filialRepository, auditoria);
    }

    @Test
    void deveRejeitarFollowUpDeOutroCliente() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID followUpId = UUID.randomUUID();
        Pessoa cliente = mock(Pessoa.class);
        ClienteFollowUp followUp = mock(ClienteFollowUp.class);
        when(cliente.isCliente()).thenReturn(true);
        when(filialRepository.existsByIdAndTenantId(filialId, tenantId)).thenReturn(true);
        when(pessoaRepository.findByIdAndTenantId(clienteId, tenantId)).thenReturn(Optional.of(cliente));
        when(followUpRepository.findByIdAndTenantId(followUpId, tenantId)).thenReturn(Optional.of(followUp));
        when(followUp.getFilialId()).thenReturn(filialId);
        when(followUp.getClienteId()).thenReturn(UUID.randomUUID());

        assertThrows(RegraNegocioException.class, () -> service.registrar(tenantId, UUID.randomUUID(), filialId,
                clienteId, followUpId, "telefone", "contato_realizado", "Retorno comercial", Instant.now()));
        verifyNoInteractions(repository, auditoria);
    }
}
