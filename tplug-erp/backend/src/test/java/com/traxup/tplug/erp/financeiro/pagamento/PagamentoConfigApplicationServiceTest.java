package com.traxup.tplug.erp.financeiro.pagamento;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentoConfigApplicationServiceTest {
    @Mock FormaPagamentoRepository formaRepository;
    @Mock CondicaoPagamentoRepository condicaoRepository;
    @Mock CondicaoPagamentoParcelaRepository parcelaRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveCriarCondicaoComPercentuaisTotalizandoCem() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        when(condicaoRepository.existsByTenantIdAndCodigoIgnoreCase(tenantId, "30D60D")).thenReturn(false);
        when(condicaoRepository.save(any(CondicaoPagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(parcelaRepository.save(any(CondicaoPagamentoParcela.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagamentoConfigApplicationService service = service();
        service.criarCondicao(tenantId, usuarioId, "30d60d", "30/60 dias", List.of(
                new PagamentoConfigApplicationService.ParcelaDefinicao(1, 30, new BigDecimal("50.0000")),
                new PagamentoConfigApplicationService.ParcelaDefinicao(2, 60, new BigDecimal("50.0000"))));

        verify(parcelaRepository, org.mockito.Mockito.times(2)).save(any(CondicaoPagamentoParcela.class));
        verify(auditoria).registrar(org.mockito.ArgumentMatchers.eq(tenantId), org.mockito.ArgumentMatchers.eq(usuarioId),
                org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq("CRIAR"), org.mockito.ArgumentMatchers.eq("CONDICAO_PAGAMENTO"),
                any(UUID.class), org.mockito.ArgumentMatchers.eq("parcelas=2"));
    }

    @Test
    void devePersistirAjustesComerciaisNaCondicao() {
        UUID tenantId = UUID.randomUUID();
        when(condicaoRepository.existsByTenantIdAndCodigoIgnoreCase(tenantId, "COMERCIAL")).thenReturn(false);
        when(condicaoRepository.save(any(CondicaoPagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(parcelaRepository.save(any(CondicaoPagamentoParcela.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service().criarCondicao(
                tenantId,
                UUID.randomUUID(),
                "comercial",
                "Comercial",
                new PagamentoConfigApplicationService.AjusteDefinicao(AjusteComercialTipo.PERCENTUAL, new BigDecimal("2.5000")),
                new PagamentoConfigApplicationService.AjusteDefinicao(AjusteComercialTipo.VALOR_FIXO, new BigDecimal("10.0000")),
                new PagamentoConfigApplicationService.AjusteDefinicao(AjusteComercialTipo.PERCENTUAL, new BigDecimal("20.0000")),
                List.of(new PagamentoConfigApplicationService.ParcelaDefinicao(1, 0, new BigDecimal("100.0000"))));

        ArgumentCaptor<CondicaoPagamento> captor = ArgumentCaptor.forClass(CondicaoPagamento.class);
        verify(condicaoRepository).save(captor.capture());
        CondicaoPagamento condicao = captor.getValue();
        assertEquals(AjusteComercialTipo.PERCENTUAL, condicao.getJurosTipo());
        assertEquals(new BigDecimal("2.5000"), condicao.getJurosValor());
        assertEquals(AjusteComercialTipo.VALOR_FIXO, condicao.getDescontoTipo());
        assertEquals(new BigDecimal("10.0000"), condicao.getDescontoValor());
        assertEquals(AjusteComercialTipo.PERCENTUAL, condicao.getEntradaTipo());
        assertEquals(new BigDecimal("20.0000"), condicao.getEntradaValor());
    }

    @Test
    void deveRejeitarAjusteSemValor() {
        UUID tenantId = UUID.randomUUID();
        when(condicaoRepository.existsByTenantIdAndCodigoIgnoreCase(tenantId, "INVALIDA")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service().criarCondicao(
                tenantId,
                UUID.randomUUID(),
                "invalida",
                "Invalida",
                new PagamentoConfigApplicationService.AjusteDefinicao(AjusteComercialTipo.PERCENTUAL, null),
                null,
                null,
                List.of(new PagamentoConfigApplicationService.ParcelaDefinicao(1, 0, new BigDecimal("100.0000")))));
        verify(condicaoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarDescontoPercentualAcimaDeCem() {
        UUID tenantId = UUID.randomUUID();
        when(condicaoRepository.existsByTenantIdAndCodigoIgnoreCase(tenantId, "DESCONTO")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service().criarCondicao(
                tenantId,
                UUID.randomUUID(),
                "desconto",
                "Desconto",
                null,
                new PagamentoConfigApplicationService.AjusteDefinicao(AjusteComercialTipo.PERCENTUAL, new BigDecimal("100.0001")),
                null,
                List.of(new PagamentoConfigApplicationService.ParcelaDefinicao(1, 0, new BigDecimal("100.0000")))));
        verify(condicaoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarEntradaPercentualAcimaDeCem() {
        UUID tenantId = UUID.randomUUID();
        when(condicaoRepository.existsByTenantIdAndCodigoIgnoreCase(tenantId, "ENTRADA")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service().criarCondicao(
                tenantId,
                UUID.randomUUID(),
                "entrada",
                "Entrada",
                null,
                null,
                new PagamentoConfigApplicationService.AjusteDefinicao(AjusteComercialTipo.PERCENTUAL, new BigDecimal("101.0000")),
                List.of(new PagamentoConfigApplicationService.ParcelaDefinicao(1, 0, new BigDecimal("100.0000")))));
        verify(condicaoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarCondicaoQuandoPercentuaisNaoSomamCem() {
        UUID tenantId = UUID.randomUUID();
        when(condicaoRepository.existsByTenantIdAndCodigoIgnoreCase(tenantId, "INVALIDA")).thenReturn(false);
        PagamentoConfigApplicationService service = service();

        assertThrows(IllegalArgumentException.class, () -> service.criarCondicao(tenantId, UUID.randomUUID(), "invalida", "Invalida", List.of(
                new PagamentoConfigApplicationService.ParcelaDefinicao(1, 30, new BigDecimal("70.0000")))));
        verify(condicaoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarNumeracaoNaoSequencial() {
        UUID tenantId = UUID.randomUUID();
        when(condicaoRepository.existsByTenantIdAndCodigoIgnoreCase(tenantId, "QUEBRA")).thenReturn(false);
        PagamentoConfigApplicationService service = service();

        assertThrows(IllegalArgumentException.class, () -> service.criarCondicao(tenantId, UUID.randomUUID(), "quebra", "Quebra", List.of(
                new PagamentoConfigApplicationService.ParcelaDefinicao(2, 30, new BigDecimal("100.0000")))));
        verify(condicaoRepository, never()).save(any());
    }

    private PagamentoConfigApplicationService service() {
        return new PagamentoConfigApplicationService(formaRepository, condicaoRepository, parcelaRepository, auditoria);
    }
}
