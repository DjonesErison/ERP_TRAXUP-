package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConciliacaoReferenciaApplicationServiceTest {
    @Mock ConciliacaoLancamentoRepository repository;
    @Mock ContaFinanceiraRepository contaRepository;
    @Mock ContaFinanceiraMovimentoRepository movimentoRepository;
    @Mock FilialRepository filialRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveNormalizarOrigemEPreservarTenantEConta() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, filialId, "Banco", "BANCO", UUID.randomUUID());
        ConciliacaoLancamento lancamento = new ConciliacaoLancamento(tenantId, filialId, contaId,
                "OFX", "fit-123", "ENTRADA", new BigDecimal("10.00"), "Credito", Instant.now(), UUID.randomUUID());
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(repository.findByTenantIdAndContaFinanceiraIdAndOrigemAndReferenciaExterna(
                tenantId, contaId, "OFX", "fit-123")).thenReturn(Optional.of(lancamento));

        var resultado = novoService().buscar(tenantId, contaId, " ofx ", " fit-123 ");

        assertEquals(lancamento, resultado);
        verify(repository).findByTenantIdAndContaFinanceiraIdAndOrigemAndReferenciaExterna(
                tenantId, contaId, "OFX", "fit-123");
    }

    @Test
    void deveRejeitarReferenciaVazia() {
        assertThrows(RegraNegocioException.class,
                () -> novoService().buscar(UUID.randomUUID(), UUID.randomUUID(), "OFX", " "));
    }

    @Test
    void deveRetornarNaoEncontradoSemRelaxarEscopo() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(repository.findByTenantIdAndContaFinanceiraIdAndOrigemAndReferenciaExterna(
                tenantId, contaId, "OFX", "ausente")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> novoService().buscar(tenantId, contaId, "OFX", "ausente"));
    }

    private ConciliacaoReferenciaApplicationService novoService() {
        ContaFinanceiraApplicationService contaService = new ContaFinanceiraApplicationService(
                contaRepository, movimentoRepository, filialRepository, auditoria);
        return new ConciliacaoReferenciaApplicationService(repository, contaService);
    }
}
