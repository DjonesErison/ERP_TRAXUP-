package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.venda.PedidoVenda;
import com.traxup.tplug.erp.venda.PedidoVendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FiscalSolicitacaoApplicationServiceTest {
    private FiscalSolicitacaoRepository repository;
    private PedidoVendaRepository pedidoRepository;
    private AuditoriaApplicationService auditoria;
    private FiscalSolicitacaoApplicationService service;

    @BeforeEach
    void setUp() {
        repository = mock(FiscalSolicitacaoRepository.class);
        pedidoRepository = mock(PedidoVendaRepository.class);
        auditoria = mock(AuditoriaApplicationService.class);
        service = new FiscalSolicitacaoApplicationService(repository, pedidoRepository, auditoria);
    }

    @Test
    void criaSolicitacaoPendenteParaVendaFaturada() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        when(pedido.getStatus()).thenReturn("FATURADO");
        when(pedido.getFilialId()).thenReturn(filialId);
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        when(repository.findByTenantIdAndPedidoVendaIdAndModeloAndAmbiente(tenantId, pedidoId, "NFCE", "PRODUCAO"))
                .thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(FiscalSolicitacao.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = service.solicitar(tenantId, usuarioId, pedidoId, "NFC-e", "producao");

        assertFalse(resultado.repetida());
        assertEquals("NFCE", resultado.solicitacao().getModelo());
        assertEquals("PRODUCAO", resultado.solicitacao().getAmbiente());
        assertEquals("PENDENTE", resultado.solicitacao().getStatus());
        assertEquals(filialId, resultado.solicitacao().getFilialId());
        verify(auditoria).registrar(eq(tenantId), eq(usuarioId), isNull(), eq(filialId),
                eq("SOLICITAR_EMISSAO"), eq("FISCAL_SOLICITACAO"), any(UUID.class), anyString());
    }

    @Test
    void replayIdenticoRetornaMesmaSolicitacaoSemDuplicar() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        when(pedido.getStatus()).thenReturn("FATURADO");
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        FiscalSolicitacao existente = new FiscalSolicitacao(tenantId, UUID.randomUUID(), pedidoId, "NFE", "HOMOLOGACAO");
        when(repository.findByTenantIdAndPedidoVendaIdAndModeloAndAmbiente(tenantId, pedidoId, "NFE", "HOMOLOGACAO"))
                .thenReturn(Optional.of(existente));

        var resultado = service.solicitar(tenantId, null, pedidoId, "NFE", "HOMOLOGACAO");

        assertTrue(resultado.repetida());
        assertSame(existente, resultado.solicitacao());
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(auditoria);
    }

    @Test
    void rejeitaPedidoQueNaoEstejaFaturado() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        when(pedido.getStatus()).thenReturn("ABERTO");
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class,
                () -> service.solicitar(tenantId, null, pedidoId, "NFCE", "PRODUCAO"));
        verifyNoInteractions(auditoria);
    }

    @Test
    void isolamentoTenantNaoAceitaPedidoDeOutroTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.solicitar(tenantId, null, pedidoId, "NFCE", "PRODUCAO"));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void iniciaProcessamentoPendenteComAuditoria() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID solicitacaoId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        FiscalSolicitacao solicitacao = new FiscalSolicitacao(
                tenantId, filialId, UUID.randomUUID(), "NFCE", "HOMOLOGACAO");
        when(repository.findByIdAndTenantId(solicitacaoId, tenantId)).thenReturn(Optional.of(solicitacao));

        var resultado = service.iniciarProcessamento(tenantId, usuarioId, solicitacaoId);

        assertFalse(resultado.repetida());
        assertEquals("PROCESSANDO", resultado.solicitacao().getStatus());
        verify(auditoria).registrar(eq(tenantId), eq(usuarioId), isNull(), eq(filialId),
                eq("INICIAR_PROCESSAMENTO"), eq("FISCAL_SOLICITACAO"), eq(solicitacao.getId()), anyString());
    }

    @Test
    void replayDoProcessamentoEIdempotenteESemNovaAuditoria() {
        UUID tenantId = UUID.randomUUID();
        UUID solicitacaoId = UUID.randomUUID();
        FiscalSolicitacao solicitacao = new FiscalSolicitacao(
                tenantId, UUID.randomUUID(), UUID.randomUUID(), "NFE", "PRODUCAO");
        solicitacao.iniciarProcessamento();
        when(repository.findByIdAndTenantId(solicitacaoId, tenantId)).thenReturn(Optional.of(solicitacao));

        var resultado = service.iniciarProcessamento(tenantId, null, solicitacaoId);

        assertTrue(resultado.repetida());
        assertEquals("PROCESSANDO", resultado.solicitacao().getStatus());
        verifyNoInteractions(auditoria);
    }

    @Test
    void processamentoNaoEncontraSolicitacaoDeOutroTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID solicitacaoId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(solicitacaoId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.iniciarProcessamento(tenantId, null, solicitacaoId));
        verifyNoInteractions(auditoria);
    }

}
