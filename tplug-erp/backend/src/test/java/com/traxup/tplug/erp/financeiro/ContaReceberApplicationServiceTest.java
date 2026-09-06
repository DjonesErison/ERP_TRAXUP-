package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaReceberApplicationServiceTest {
    @Mock ContaReceberRepository repository;
    @Mock ContaReceberRecebimentoRepository recebimentoRepository;
    @Mock FilialRepository filialRepository;
    @Mock PessoaRepository pessoaRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveBuscarContaSempreNoTenantInformado() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.empty());

        ContaReceberApplicationService service = service();

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(tenantId, contaId));
        verify(repository).findByIdAndTenantId(contaId, tenantId);
    }

    @Test
    void deveRegistrarRecebimentoParcialComLockTenantScoped() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        BigDecimal valor = new BigDecimal("40.0000");
        ContaReceber conta = new ContaReceber(tenantId, filialId, UUID.randomUUID(), "R-LOCK", "Titulo",
                new BigDecimal("100.0000"), LocalDate.now().plusDays(5), usuarioId);
        when(repository.findByIdAndTenantIdForUpdate(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(recebimentoRepository.save(any(ContaReceberRecebimento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.save(conta)).thenReturn(conta);

        ContaReceber resultado = service().receber(tenantId, usuarioId, contaId, valor);

        assertEquals("PARCIAL", resultado.getStatus());
        assertEquals(0, resultado.getValorRecebido().compareTo(valor));
        verify(repository).findByIdAndTenantIdForUpdate(contaId, tenantId);
        verify(recebimentoRepository).save(any(ContaReceberRecebimento.class));
    }

    @Test
    void deveListarOrigemSomenteNoTenantInformadoENormalizarTipo() {
        UUID tenantId = UUID.randomUUID();
        UUID origemId = UUID.randomUUID();
        when(repository.findAllByTenantIdAndOrigemTipoAndOrigemIdOrderByVencimentoAscCriadoEmDesc(
                tenantId, "PEDIDO_VENDA", origemId)).thenReturn(List.of());

        ContaReceberApplicationService service = service();

        service.listarPorOrigem(tenantId, " pedido_venda ", origemId);

        verify(repository).findAllByTenantIdAndOrigemTipoAndOrigemIdOrderByVencimentoAscCriadoEmDesc(
                tenantId, "PEDIDO_VENDA", origemId);
    }

    @Test
    void naoDeveListarRecebimentosDeContaForaDoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.empty());

        ContaReceberApplicationService service = service();

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.listarRecebimentos(tenantId, contaId));
        verify(repository).findByIdAndTenantId(contaId, tenantId);
        verifyNoInteractions(recebimentoRepository);
    }

    private ContaReceberApplicationService service() {
        return new ContaReceberApplicationService(
                repository, recebimentoRepository, filialRepository, pessoaRepository, auditoria);
    }
}
