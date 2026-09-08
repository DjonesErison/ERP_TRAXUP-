package com.traxup.tplug.erp.crm;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.Pessoa;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteFollowUpApplicationServiceTest {
    @Mock ClienteFollowUpRepository repository;
    @Mock PessoaRepository pessoaRepository;
    @Mock FilialRepository filialRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveListarPendentesComEscopoTenantELimitePadrao() {
        UUID tenantId = UUID.randomUUID();
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(repository.buscar(eq(tenantId), eq(null), eq(null), eq("PENDENTE"), any(Pageable.class)))
                .thenReturn(List.of());
        var service = service();

        service.listar(tenantId, null, null, null, null);

        verify(repository).buscar(eq(tenantId), eq(null), eq(null), eq("PENDENTE"), pageable.capture());
        assertEquals(100, pageable.getValue().getPageSize());
        verifyNoInteractions(filialRepository, pessoaRepository);
    }

    @Test
    void deveRejeitarFiltroInvalidoAntesDosRepositorios() {
        var service = service();

        assertThrows(RegraNegocioException.class,
                () -> service.listar(UUID.randomUUID(), null, null, "INVALIDO", 100));
        assertThrows(RegraNegocioException.class,
                () -> service.listar(UUID.randomUUID(), null, null, "PENDENTE", 501));

        verifyNoInteractions(repository, pessoaRepository, filialRepository, auditoria);
    }

    @Test
    void deveCriarFollowUpSomenteParaClienteAtivoDaFilialDoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        Pessoa cliente = org.mockito.Mockito.mock(Pessoa.class);
        when(filialRepository.existsByIdAndTenantId(filialId, tenantId)).thenReturn(true);
        when(pessoaRepository.findByIdAndTenantId(clienteId, tenantId)).thenReturn(Optional.of(cliente));
        when(cliente.isCliente()).thenReturn(true);
        when(cliente.isAtivo()).thenReturn(true);
        when(repository.save(any(ClienteFollowUp.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var service = service();

        ClienteFollowUp criado = service.criar(tenantId, usuarioId, filialId, clienteId,
                " Retornar contato ", " Cliente pediu retorno ", Instant.now().plusSeconds(3600));

        assertEquals("PENDENTE", criado.getStatus());
        assertEquals("Retornar contato", criado.getAssunto());
        assertEquals("Cliente pediu retorno", criado.getObservacao());
        verify(auditoria).registrar(tenantId, usuarioId, null, filialId, "CRIAR",
                "CRM_CLIENTE_FOLLOWUP", criado.getId(), "clienteId=" + clienteId);
    }

    @Test
    void deveRejeitarFilialCrossTenantAntesDeConsultarCliente() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        when(filialRepository.existsByIdAndTenantId(filialId, tenantId)).thenReturn(false);
        var service = service();

        assertThrows(RecursoNaoEncontradoException.class, () -> service.criar(
                tenantId, UUID.randomUUID(), filialId, UUID.randomUUID(), "Contato", null, Instant.now()));

        verifyNoInteractions(pessoaRepository, repository, auditoria);
    }

    @Test
    void deveConcluirComBloqueioERegistrarAuditoriaSemObservacao() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        ClienteFollowUp followUp = new ClienteFollowUp(tenantId, filialId, clienteId,
                "Contato", "texto livre", Instant.now(), UUID.randomUUID());
        when(repository.buscarParaAtualizar(followUp.getId(), tenantId)).thenReturn(Optional.of(followUp));
        when(repository.save(followUp)).thenReturn(followUp);
        var service = service();

        ClienteFollowUp concluido = service.concluir(tenantId, usuarioId, followUp.getId());

        assertEquals("CONCLUIDO", concluido.getStatus());
        assertEquals(usuarioId, concluido.getConcluidoPorId());
        verify(repository).buscarParaAtualizar(followUp.getId(), tenantId);
        verify(auditoria).registrar(tenantId, usuarioId, null, filialId, "CONCLUIR",
                "CRM_CLIENTE_FOLLOWUP", followUp.getId(), "clienteId=" + clienteId);
    }

    @Test
    void naoDevePermitirSegundaTransicaoDepoisDeConcluido() {
        UUID tenantId = UUID.randomUUID();
        ClienteFollowUp followUp = new ClienteFollowUp(tenantId, UUID.randomUUID(), UUID.randomUUID(),
                "Contato", null, Instant.now(), UUID.randomUUID());
        followUp.concluir(UUID.randomUUID());
        when(repository.buscarParaAtualizar(followUp.getId(), tenantId)).thenReturn(Optional.of(followUp));
        var service = service();

        assertThrows(RegraNegocioException.class,
                () -> service.cancelar(tenantId, UUID.randomUUID(), followUp.getId()));
        verify(repository, never()).save(followUp);
        verifyNoInteractions(auditoria);
    }

    private ClienteFollowUpApplicationService service() {
        return new ClienteFollowUpApplicationService(repository, pessoaRepository, filialRepository, auditoria);
    }
}
