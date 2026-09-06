package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaPagarApplicationServiceTest {
    @Mock ContaPagarRepository repository;
    @Mock ContaPagarPagamentoRepository pagamentoRepository;
    @Mock FilialRepository filialRepository;
    @Mock PessoaRepository pessoaRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveRegistrarPagamentoParcialComHistoricoAuditoriaELockTenantScoped() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        BigDecimal valor = new BigDecimal("35.0000");
        ContaPagar conta = new ContaPagar(tenantId, filialId, UUID.randomUUID(), "P-1", "Titulo",
                new BigDecimal("100.0000"), LocalDate.now().plusDays(5), usuarioId);

        when(repository.findByIdAndTenantIdForUpdate(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(pagamentoRepository.save(any(ContaPagarPagamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.save(conta)).thenReturn(conta);

        ContaPagar resultado = service().pagar(tenantId, usuarioId, contaId, valor);

        assertEquals("PARCIAL", resultado.getStatus());
        assertEquals(0, resultado.getValorPago().compareTo(valor));
        verify(repository).findByIdAndTenantIdForUpdate(contaId, tenantId);
        verify(pagamentoRepository).save(any(ContaPagarPagamento.class));
        verify(auditoria).registrar(
                org.mockito.ArgumentMatchers.eq(tenantId), org.mockito.ArgumentMatchers.eq(usuarioId),
                org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.eq(filialId),
                org.mockito.ArgumentMatchers.eq("BAIXAR"), org.mockito.ArgumentMatchers.eq("CONTA_PAGAR"),
                org.mockito.ArgumentMatchers.eq(conta.getId()), org.mockito.ArgumentMatchers.contains("status=PARCIAL"));
    }

    @Test
    void deveValidarTituloNoTenantAntesDeConsultarHistorico() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaPagar conta = new ContaPagar(tenantId, UUID.randomUUID(), UUID.randomUUID(), "P-2", "Titulo",
                new BigDecimal("100.0000"), LocalDate.now().plusDays(5), UUID.randomUUID());
        when(repository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(pagamentoRepository.findAllByTenantIdAndContaPagarIdOrderByPagoEmDesc(tenantId, contaId))
                .thenReturn(List.of());

        service().listarPagamentos(tenantId, contaId);

        verify(repository).findByIdAndTenantId(contaId, tenantId);
        verify(pagamentoRepository).findAllByTenantIdAndContaPagarIdOrderByPagoEmDesc(tenantId, contaId);
    }

    private ContaPagarApplicationService service() {
        return new ContaPagarApplicationService(
                repository, pagamentoRepository, filialRepository, pessoaRepository, auditoria);
    }
}
