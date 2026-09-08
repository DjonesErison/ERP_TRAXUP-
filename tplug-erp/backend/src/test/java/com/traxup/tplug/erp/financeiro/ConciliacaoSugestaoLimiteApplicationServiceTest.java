package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConciliacaoSugestaoLimiteApplicationServiceTest {
    @Mock ConciliacaoLancamentoRepository repository;
    @Mock ContaFinanceiraRepository contaRepository;
    @Mock ContaFinanceiraMovimentoRepository movimentoRepository;
    @Mock FilialRepository filialRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveAplicarLimiteSolicitadoPreservandoEscopoDoMatching() {
        UUID tenantId = UUID.randomUUID();
        UUID lancamentoId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        Instant ocorridoEm = Instant.parse("2026-09-08T12:00:00Z");
        BigDecimal valor = new BigDecimal("149.90");
        ConciliacaoLancamento lancamento = new ConciliacaoLancamento(
                tenantId, filialId, contaId, "OFX", "REF-LIMITE", "ENTRADA", valor,
                "Credito", ocorridoEm, UUID.randomUUID());
        when(repository.findByIdAndTenantId(lancamentoId, tenantId)).thenReturn(Optional.of(lancamento));
        when(movimentoRepository.findCandidatosDisponiveis(
                tenantId, contaId, filialId, "ENTRADA", valor,
                ocorridoEm.minusSeconds(259200), ocorridoEm.plusSeconds(259200), PageRequest.of(0, 15)))
                .thenReturn(List.of());

        List<ContaFinanceiraMovimento> resultado = novoService().sugerirMovimentos(tenantId, lancamentoId, 15);

        assertEquals(0, resultado.size());
        verify(movimentoRepository).findCandidatosDisponiveis(
                tenantId, contaId, filialId, "ENTRADA", valor,
                ocorridoEm.minusSeconds(259200), ocorridoEm.plusSeconds(259200), PageRequest.of(0, 15));
    }

    @Test
    void deveRejeitarLimiteAcimaDoMaximoAntesDaConsultaDeMovimentos() {
        UUID tenantId = UUID.randomUUID();
        UUID lancamentoId = UUID.randomUUID();
        ConciliacaoLancamento lancamento = new ConciliacaoLancamento(
                tenantId, UUID.randomUUID(), UUID.randomUUID(), "OFX", "REF-INVALIDA", "SAIDA",
                new BigDecimal("20.00"), "Debito", Instant.parse("2026-09-08T12:00:00Z"), UUID.randomUUID());
        when(repository.findByIdAndTenantId(lancamentoId, tenantId)).thenReturn(Optional.of(lancamento));

        assertThrows(RegraNegocioException.class,
                () -> novoService().sugerirMovimentos(tenantId, lancamentoId, 101));

        verifyNoInteractions(movimentoRepository);
    }

    private ConciliacaoApplicationService novoService() {
        ContaFinanceiraApplicationService contaService = new ContaFinanceiraApplicationService(
                contaRepository, movimentoRepository, filialRepository, auditoria);
        return new ConciliacaoApplicationService(repository, contaService, movimentoRepository, auditoria);
    }
}
