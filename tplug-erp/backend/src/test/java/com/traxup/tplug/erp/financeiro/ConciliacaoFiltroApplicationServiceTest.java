package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConciliacaoFiltroApplicationServiceTest {
    @Mock ConciliacaoLancamentoRepository repository;
    @Mock ContaFinanceiraRepository contaRepository;
    @Mock ContaFinanceiraMovimentoRepository movimentoRepository;
    @Mock FilialRepository filialRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveNormalizarFiltrosEPreservarTenantEConta() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
        Instant inicio = Instant.parse("2026-09-01T00:00:00Z");
        Instant fim = Instant.parse("2026-09-06T23:59:59Z");
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(repository.filtrar(tenantId, contaId, "OFX", "TAXA", "PENDENTE", null, inicio, fim)).thenReturn(List.of());

        novoService().listar(tenantId, contaId, " ofx ", "taxa", "pendente", inicio, fim);

        verify(repository).filtrar(tenantId, contaId, "OFX", "TAXA", "PENDENTE", null, inicio, fim);
    }

    @Test
    void deveNormalizarTipoNaListagem() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(repository.filtrar(tenantId, contaId, null, null, null, "SAIDA", null, null)).thenReturn(List.of());

        novoService().listar(tenantId, contaId, null, null, null, " saida ", null, null);

        verify(repository).filtrar(tenantId, contaId, null, null, null, "SAIDA", null, null);
    }

    @Test
    void deveNormalizarFiltrosDoResumoEPreservarTenantEConta() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
        Instant inicio = Instant.parse("2026-09-01T00:00:00Z");
        Instant fim = Instant.parse("2026-09-06T23:59:59Z");
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));

        novoService().resumir(tenantId, contaId, " ofx ", "taxa", "pendente", inicio, fim);

        verify(repository).resumirFiltrado(tenantId, contaId, "OFX", "TAXA", "PENDENTE", null, inicio, fim);
    }

    @Test
    void deveNormalizarTipoNoResumo() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));

        novoService().resumir(tenantId, contaId, null, null, null, " entrada ", null, null);

        verify(repository).resumirFiltrado(tenantId, contaId, null, null, null, "ENTRADA", null, null);
    }

    @Test
    void deveRejeitarPeriodoInvertidoAntesDaConsulta() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));

        assertThrows(RegraNegocioException.class, () -> novoService().listar(
                tenantId, contaId, null, null, null,
                Instant.parse("2026-09-06T00:00:00Z"), Instant.parse("2026-09-01T00:00:00Z")));
    }

    @Test
    void deveRejeitarPeriodoInvertidoNoResumoAntesDaConsulta() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));

        assertThrows(RegraNegocioException.class, () -> novoService().resumir(
                tenantId, contaId, null, null, null,
                Instant.parse("2026-09-06T00:00:00Z"), Instant.parse("2026-09-01T00:00:00Z")));
    }

    private ConciliacaoApplicationService novoService() {
        ContaFinanceiraApplicationService contaService = new ContaFinanceiraApplicationService(
                contaRepository, movimentoRepository, filialRepository, auditoria);
        return new ConciliacaoApplicationService(repository, contaService, movimentoRepository, auditoria);
    }
}
