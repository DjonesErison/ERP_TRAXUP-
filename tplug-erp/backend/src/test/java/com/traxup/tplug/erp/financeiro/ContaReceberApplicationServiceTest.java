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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaReceberApplicationServiceTest {
    @Mock ContaReceberRepository repository;
    @Mock ContaReceberMovimentoRepository movimentoRepository;
    @Mock FilialRepository filialRepository;
    @Mock PessoaRepository pessoaRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveBuscarContaSempreNoTenantInformado() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.empty());

        ContaReceberApplicationService service = novoService();

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(tenantId, contaId));
        verify(repository).findByIdAndTenantId(contaId, tenantId);
    }

    @Test
    void deveRegistrarMovimentoNoMesmoTenantEAtualizarSaldo() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        ContaReceber conta = novaConta(tenantId);
        when(repository.findByIdAndTenantIdForUpdate(conta.getId(), tenantId)).thenReturn(Optional.of(conta));
        when(movimentoRepository.save(any(ContaReceberMovimento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ContaReceberMovimento movimento = novoService().registrarRecebimento(
                tenantId, usuarioId, conta.getId(), new BigDecimal("25.00"), LocalDate.now(), "PIX");

        assertEquals(0, new BigDecimal("25.00").compareTo(movimento.getValor()));
        assertEquals(tenantId, movimento.getTenantId());
        assertEquals("PARCIALMENTE_RECEBIDO", conta.getStatus());
        assertEquals(0, new BigDecimal("75.00").compareTo(conta.saldoAberto()));
        verify(repository).findByIdAndTenantIdForUpdate(conta.getId(), tenantId);
        verify(movimentoRepository).save(any(ContaReceberMovimento.class));
    }

    private ContaReceberApplicationService novoService() {
        return new ContaReceberApplicationService(
                repository, movimentoRepository, filialRepository, pessoaRepository, auditoria);
    }

    private ContaReceber novaConta(UUID tenantId) {
        return new ContaReceber(
                tenantId, UUID.randomUUID(), UUID.randomUUID(), "DOC-001", "Venda a prazo",
                new BigDecimal("100.00"), LocalDate.now().plusDays(30), UUID.randomUUID());
    }
}
