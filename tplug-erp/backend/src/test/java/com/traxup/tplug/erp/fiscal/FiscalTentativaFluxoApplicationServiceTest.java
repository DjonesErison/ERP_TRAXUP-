package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FiscalTentativaFluxoApplicationServiceTest {
    @Test
    void executaAsQuatroEtapasNaOrdem() {
        var xml = mock(FiscalTentativaXmlApplicationService.class);
        var assinatura = mock(FiscalTentativaAssinaturaApplicationService.class);
        var transmissao = mock(FiscalTentativaTransmissaoApplicationService.class);
        var processado = mock(FiscalTentativaProcessadoApplicationService.class);
        var falhas = mock(FiscalTentativaFalhaApplicationService.class);
        var service = new FiscalTentativaFluxoApplicationService(
                xml, assinatura, transmissao, processado, falhas);
        UUID tenant = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();
        UUID tentativa = UUID.randomUUID();
        UUID documento = UUID.randomUUID();
        UUID xmlId = UUID.randomUUID();
        UUID assinaturaId = UUID.randomUUID();
        UUID transmissaoId = UUID.randomUUID();
        UUID processadoId = UUID.randomUUID();

        when(xml.gerar(tenant, usuario, tentativa)).thenReturn(
                new FiscalTentativaXmlApplicationService.Resultado(
                        xmlId, documento, tentativa, 2, "1.2", "a".repeat(64), false));
        when(assinatura.assinar(tenant, usuario, tentativa)).thenReturn(
                new FiscalTentativaAssinaturaApplicationService.Resultado(
                        assinaturaId, documento, tentativa, 2, xmlId, UUID.randomUUID(),
                        "SIMULADA", "SHA256withRSA-SIMULADO", "b".repeat(64), false));
        when(transmissao.transmitir(tenant, usuario, tentativa)).thenReturn(
                new FiscalTentativaTransmissaoApplicationService.Resultado(
                        transmissaoId, documento, tentativa, 2, assinaturaId,
                        "AUTORIZADO_SIMULADO", "100-SIM", "SIM-123",
                        "c".repeat(64), false));
        when(processado.gerar(tenant, usuario, tentativa)).thenReturn(
                new FiscalTentativaProcessadoApplicationService.Resultado(
                        processadoId, documento, tentativa, 2, transmissaoId,
                        "SIM-123", "AUTORIZADO_SIMULADO", "PROCESSADO_SIMULADO",
                        "1.0", "d".repeat(64), "<processado/>", false));

        var resultado = service.processar(tenant, usuario, tentativa);

        InOrder ordem = inOrder(falhas, xml, assinatura, transmissao, processado);
        ordem.verify(falhas).prepararRetomada(tenant, tentativa);
        ordem.verify(xml).gerar(tenant, usuario, tentativa);
        ordem.verify(assinatura).assinar(tenant, usuario, tentativa);
        ordem.verify(transmissao).transmitir(tenant, usuario, tentativa);
        ordem.verify(processado).gerar(tenant, usuario, tentativa);
        assertEquals(processadoId, resultado.processadoId());
        assertEquals("SIM-123", resultado.protocolo());
    }
    @Test
    void registraEtapaQueFalhouSemEngolirErroOriginal() {
        var xml = mock(FiscalTentativaXmlApplicationService.class);
        var assinatura = mock(FiscalTentativaAssinaturaApplicationService.class);
        var transmissao = mock(FiscalTentativaTransmissaoApplicationService.class);
        var processado = mock(FiscalTentativaProcessadoApplicationService.class);
        var falhas = mock(FiscalTentativaFalhaApplicationService.class);
        var service = new FiscalTentativaFluxoApplicationService(
                xml, assinatura, transmissao, processado, falhas);
        UUID tenant = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();
        UUID tentativa = UUID.randomUUID();
        UUID documento = UUID.randomUUID();
        RuntimeException erro = new IllegalStateException("falha controlada");

        when(xml.gerar(tenant, usuario, tentativa)).thenReturn(
                new FiscalTentativaXmlApplicationService.Resultado(
                        UUID.randomUUID(), documento, tentativa, 1,
                        "1.2", "a".repeat(64), false));
        when(assinatura.assinar(tenant, usuario, tentativa)).thenThrow(erro);

        assertThrows(IllegalStateException.class,
                () -> service.processar(tenant, usuario, tentativa));
        verify(falhas).registrar(tenant, tentativa,
                FiscalTentativaFalhaApplicationService.Etapa.ASSINATURA, erro);
    }

}
