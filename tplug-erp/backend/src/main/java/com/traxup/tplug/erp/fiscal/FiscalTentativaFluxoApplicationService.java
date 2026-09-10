package com.traxup.tplug.erp.fiscal;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FiscalTentativaFluxoApplicationService {
    private final FiscalTentativaXmlApplicationService xml;
    private final FiscalTentativaAssinaturaApplicationService assinatura;
    private final FiscalTentativaTransmissaoApplicationService transmissao;
    private final FiscalTentativaProcessadoApplicationService processado;
    private final FiscalTentativaFalhaApplicationService falhas;

    public FiscalTentativaFluxoApplicationService(
            FiscalTentativaXmlApplicationService xml,
            FiscalTentativaAssinaturaApplicationService assinatura,
            FiscalTentativaTransmissaoApplicationService transmissao,
            FiscalTentativaProcessadoApplicationService processado,
            FiscalTentativaFalhaApplicationService falhas) {
        this.xml = xml;
        this.assinatura = assinatura;
        this.transmissao = transmissao;
        this.processado = processado;
        this.falhas = falhas;
    }

    public Resultado processar(UUID tenantId, UUID usuarioId, UUID tentativaId) {
        falhas.prepararRetomada(tenantId, tentativaId);
        var etapa = FiscalTentativaFalhaApplicationService.Etapa.XML;
        try {
            var x = xml.gerar(tenantId, usuarioId, tentativaId);
            etapa = FiscalTentativaFalhaApplicationService.Etapa.ASSINATURA;
            var a = assinatura.assinar(tenantId, usuarioId, tentativaId);
            etapa = FiscalTentativaFalhaApplicationService.Etapa.TRANSMISSAO;
            var t = transmissao.transmitir(tenantId, usuarioId, tentativaId);
            etapa = FiscalTentativaFalhaApplicationService.Etapa.PROCESSADO;
            var p = processado.gerar(tenantId, usuarioId, tentativaId);
            return new Resultado(tentativaId, x.documentoId(), x.tentativaNumero(),
                    x.xmlId(), a.assinaturaId(), t.transmissaoId(), p.processadoId(),
                    t.status(), t.codigoResposta(), t.protocolo(), p.hashSha256(),
                    new Etapas(x.repetida(), a.repetida(), t.repetida(), p.repetida()));
        } catch (RuntimeException erro) {
            falhas.registrar(tenantId, tentativaId, etapa, erro);
            throw erro;
        }
    }

    public record Resultado(UUID tentativaId, UUID documentoId, int tentativaNumero,
                            UUID xmlId, UUID assinaturaId, UUID transmissaoId,
                            UUID processadoId, String status, String codigoResposta,
                            String protocolo, String processadoHashSha256,
                            Etapas etapas) {}

    public record Etapas(boolean xmlRepetido, boolean assinaturaRepetida,
                         boolean transmissaoRepetida, boolean processadoRepetido) {}
}
