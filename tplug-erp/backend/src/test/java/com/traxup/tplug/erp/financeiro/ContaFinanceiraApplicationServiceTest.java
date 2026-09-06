package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaFinanceiraApplicationServiceTest {
    @Mock ContaFinanceiraRepository repository;
    @Mock ContaFinanceiraMovimentoRepository movimentoRepository;
    @Mock FilialRepository filialRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveBuscarContaSempreNoTenantInformado() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service().buscar(tenantId, contaId));
        verify(repository).findByIdAndTenantId(contaId, tenantId);
    }

    @Test
    void deveUsarLockTenantScopedAoMovimentarConta() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Caixa", "CAIXA", usuarioId);
        when(repository.findByIdAndTenantIdForUpdate(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(repository.save(conta)).thenReturn(conta);
        when(movimentoRepository.save(any(ContaFinanceiraMovimento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ContaFinanceira atualizada = service().movimentar(
                tenantId, usuarioId, contaId, "ENTRADA", new BigDecimal("125.00"), "Aporte");

        assertEquals(new BigDecimal("125.00"), atualizada.getSaldo());
        verify(repository).findByIdAndTenantIdForUpdate(contaId, tenantId);
    }

    @Test
    void deveUsarLockTenantScopedAoDesativarConta() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", usuarioId);
        when(repository.findByIdAndTenantIdForUpdate(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(repository.save(conta)).thenReturn(conta);

        ContaFinanceira atualizada = service().desativar(tenantId, usuarioId, contaId);

        assertEquals(false, atualizada.isAtivo());
        verify(repository).findByIdAndTenantIdForUpdate(contaId, tenantId);
    }

    private ContaFinanceiraApplicationService service() {
        return new ContaFinanceiraApplicationService(repository, movimentoRepository, filialRepository, auditoria);
    }
}
