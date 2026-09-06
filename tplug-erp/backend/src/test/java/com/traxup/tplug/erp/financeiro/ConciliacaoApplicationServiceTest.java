package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConciliacaoApplicationServiceTest {
    @Mock ConciliacaoLancamentoRepository repository;
    @Mock ContaFinanceiraRepository contaRepository;
    @Mock ContaFinanceiraMovimentoRepository movimentoRepository;
    @Mock FilialRepository filialRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveImportarLancamentoUsandoContaDoMesmoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = novaConta(tenantId);
        prepararContaParaImportacao(tenantId, contaId, conta);
        when(repository.save(any(ConciliacaoLancamento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        novoService().importar(tenantId, UUID.randomUUID(), contaId, "ofx", "REF-1", "entrada",
                new BigDecimal("50.00"), "Credito", Instant.now());

        verify(repository).bloquearContaParaImportacao(tenantId, contaId);
        verify(repository).save(any(ConciliacaoLancamento.class));
    }

    @Test
    void deveRetornarLancamentoExistenteQuandoRequisicaoForIdempotente() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        Instant ocorridoEm = Instant.parse("2026-09-06T03:00:00Z");
        ContaFinanceira conta = novaConta(tenantId);
        ConciliacaoLancamento existente = new ConciliacaoLancamento(
                tenantId, conta.getFilialId(), contaId, "OFX", "REF-IDEM", "ENTRADA",
                new BigDecimal("50.00"), "Credito", ocorridoEm, UUID.randomUUID());
        prepararContaParaImportacao(tenantId, contaId, conta);
        when(repository.findByTenantIdAndContaFinanceiraIdAndOrigemAndReferenciaExterna(
                tenantId, contaId, "OFX", "REF-IDEM")).thenReturn(Optional.of(existente));

        ConciliacaoLancamento resultado = novoService().importar(
                tenantId, UUID.randomUUID(), contaId, "ofx", "REF-IDEM", "entrada",
                new BigDecimal("50.0000"), "Credito", ocorridoEm);

        assertSame(existente, resultado);
        verify(repository, never()).save(any(ConciliacaoLancamento.class));
        verify(auditoria, never()).registrar(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deveRejeitarMesmaReferenciaComConteudoDiferente() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        Instant ocorridoEm = Instant.parse("2026-09-06T03:00:00Z");
        ContaFinanceira conta = novaConta(tenantId);
        ConciliacaoLancamento existente = new ConciliacaoLancamento(
                tenantId, conta.getFilialId(), contaId, "OFX", "REF-CONFLITO", "ENTRADA",
                new BigDecimal("50.00"), "Credito", ocorridoEm, UUID.randomUUID());
        prepararContaParaImportacao(tenantId, contaId, conta);
        when(repository.findByTenantIdAndContaFinanceiraIdAndOrigemAndReferenciaExterna(
                tenantId, contaId, "OFX", "REF-CONFLITO")).thenReturn(Optional.of(existente));

        assertThrows(RecursoConflitanteException.class, () -> novoService().importar(
                tenantId, UUID.randomUUID(), contaId, "ofx", "REF-CONFLITO", "entrada",
                new BigDecimal("51.00"), "Credito", ocorridoEm));

        verify(repository, never()).save(any(ConciliacaoLancamento.class));
    }

    @Test
    void deveImportarLoteNaMesmaContaDoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = novaConta(tenantId);
        prepararContaParaImportacao(tenantId, contaId, conta);
        when(repository.save(any(ConciliacaoLancamento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        var itens = List.of(
                new ConciliacaoApplicationService.ImportacaoLancamento("api", "L1", "entrada", new BigDecimal("10.00"), "Credito 1", Instant.now()),
                new ConciliacaoApplicationService.ImportacaoLancamento("api", "L2", "saida", new BigDecimal("5.00"), "Debito 1", Instant.now())
        );

        List<ConciliacaoLancamento> resultado = novoService().importarLote(tenantId, UUID.randomUUID(), contaId, itens);

        assertEquals(2, resultado.size());
        verify(contaRepository, times(2)).findByIdAndTenantId(contaId, tenantId);
        verify(repository, times(2)).bloquearContaParaImportacao(tenantId, contaId);
        verify(repository, times(2)).save(any(ConciliacaoLancamento.class));
    }

    @Test
    void deveSugerirMovimentosCompativeisEmJanelaDeTresDias() {
        UUID tenantId = UUID.randomUUID();
        UUID lancamentoId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        Instant ocorridoEm = Instant.parse("2026-09-05T12:00:00Z");
        BigDecimal valor = new BigDecimal("125.50");
        ConciliacaoLancamento lancamento = new ConciliacaoLancamento(
                tenantId, filialId, contaId, "OFX", "REF-SUG", "ENTRADA", valor,
                "Credito", ocorridoEm, UUID.randomUUID());
        ContaFinanceiraMovimento movimento = new ContaFinanceiraMovimento(
                tenantId, filialId, contaId, "ENTRADA", valor, "Recebimento", UUID.randomUUID());
        when(repository.findByIdAndTenantId(lancamentoId, tenantId)).thenReturn(Optional.of(lancamento));
        when(movimentoRepository.findCandidatosDisponiveis(
                tenantId, contaId, filialId, "ENTRADA", valor,
                ocorridoEm.minusSeconds(259200), ocorridoEm.plusSeconds(259200)))
                .thenReturn(List.of(movimento));

        List<ContaFinanceiraMovimento> sugestoes = novoService().sugerirMovimentos(tenantId, lancamentoId);

        assertEquals(1, sugestoes.size());
        verify(movimentoRepository).findCandidatosDisponiveis(
                tenantId, contaId, filialId, "ENTRADA", valor,
                ocorridoEm.minusSeconds(259200), ocorridoEm.plusSeconds(259200));
    }

    @Test
    void naoDeveConciliarMovimentoDeOutraConta() {
        UUID tenantId = UUID.randomUUID();
        UUID lancamentoId = UUID.randomUUID();
        UUID movimentoId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        ConciliacaoLancamento lancamento = new ConciliacaoLancamento(
                tenantId, filialId, UUID.randomUUID(), "OFX", "REF-2", "ENTRADA",
                new BigDecimal("75.00"), "Credito", Instant.now(), UUID.randomUUID());
        ContaFinanceiraMovimento movimento = new ContaFinanceiraMovimento(
                tenantId, filialId, UUID.randomUUID(), "ENTRADA", new BigDecimal("75.00"), "Movimento", UUID.randomUUID());
        when(repository.findByIdAndTenantIdForUpdate(lancamentoId, tenantId)).thenReturn(Optional.of(lancamento));
        when(movimentoRepository.findByIdAndTenantIdForUpdate(movimentoId, tenantId)).thenReturn(Optional.of(movimento));

        assertThrows(RegraNegocioException.class,
                () -> novoService().conciliar(tenantId, UUID.randomUUID(), lancamentoId, movimentoId));
        verify(repository).findByIdAndTenantIdForUpdate(lancamentoId, tenantId);
        verify(movimentoRepository).findByIdAndTenantIdForUpdate(movimentoId, tenantId);
    }

    @Test
    void naoDeveReutilizarMovimentoEmOutraConciliacao() {
        UUID tenantId = UUID.randomUUID();
        UUID lancamentoId = UUID.randomUUID();
        UUID movimentoId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ConciliacaoLancamento lancamento = new ConciliacaoLancamento(
                tenantId, filialId, contaId, "OFX", "REF-3", "ENTRADA",
                new BigDecimal("75.00"), "Credito", Instant.now(), UUID.randomUUID());
        ContaFinanceiraMovimento movimento = new ContaFinanceiraMovimento(
                tenantId, filialId, contaId, "ENTRADA", new BigDecimal("75.00"), "Movimento", UUID.randomUUID());
        when(repository.findByIdAndTenantIdForUpdate(lancamentoId, tenantId)).thenReturn(Optional.of(lancamento));
        when(movimentoRepository.findByIdAndTenantIdForUpdate(movimentoId, tenantId)).thenReturn(Optional.of(movimento));
        when(repository.existsByTenantIdAndMovimentoIdAndIdNot(tenantId, movimentoId, lancamentoId)).thenReturn(true);

        assertThrows(RecursoConflitanteException.class,
                () -> novoService().conciliar(tenantId, UUID.randomUUID(), lancamentoId, movimentoId));
        verify(repository, never()).save(any(ConciliacaoLancamento.class));
    }

    private void prepararContaParaImportacao(UUID tenantId, UUID contaId, ContaFinanceira conta) {
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(repository.bloquearContaParaImportacao(tenantId, contaId)).thenReturn(Optional.of(contaId));
    }

    private ContaFinanceira novaConta(UUID tenantId) {
        return new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
    }

    private ConciliacaoApplicationService novoService() {
        ContaFinanceiraApplicationService contaService = new ContaFinanceiraApplicationService(
                contaRepository, movimentoRepository, filialRepository, auditoria);
        return new ConciliacaoApplicationService(repository, contaService, movimentoRepository, auditoria);
    }
}
