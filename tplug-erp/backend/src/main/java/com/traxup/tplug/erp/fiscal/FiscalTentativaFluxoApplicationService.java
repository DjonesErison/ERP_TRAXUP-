package com.traxup.tplug.erp.fiscal;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FiscalTentativaFluxoApplicationService {
    private final FiscalTentativaXmlApplicationService xml;
    private final FiscalTentativaAssinaturaApplicationService assinatura;
    private final FiscalTentativaTransmissaoApplicationService transmissao;
    private final FiscalTentativaProcessadoApplicationService processado;

    public FiscalTentativaFluxoApplicationService(
            FiscalTentativaXmlApplicationService xml,
            FiscalTentativaAssinaturaApplicationService assinatura,
            FiscalTentativaTransmissaoApplicationService transmissao,
            FiscalTentativaProcessadoApplicationService processado) {
        this.xml = xml;
        this.assinatura = assinatura;
        this.transmissao = transmissao;
        this.processado = processado;
    }

    public Resultado processar(UUID tenantId, UUID usuarioId, UUID tentativaId) {
        var x = xml.gerar(tenantId, usuarioId, tentativaId);
        var a = assinatura.assinar(tenantId, usuarioId, tentativaId);
        var t = transmissao.transmitir(tenantId, usuarioId, tentativaId);
        var p = processado.gerar(tenantId, usuarioId, tentativaId);
        return new Resultado(tentativaId, x.documentoId(), x.tentativaNumero(),
                x.xmlId(), a.assinaturaId(), t.transmissaoId(), p.processadoId(),
                t.status(), t.codigoResposta(), t.protocolo(), p.hashSha256(),
                new Etapas(x.repetida(), a.repetida(), t.repetida(), p.repetida()));
    }

    public record Resultado(UUID tentativaId, UUID documentoId, int tentativaNumero,
                            UUID xmlId, UUID assinaturaId, UUID transmissaoId,
                            UUID processadoId, String status, String codigoResposta,
                            String protocolo, String processadoHashSha256,
                            Etapas etapas) {}

    public record Etapas(boolean xmlRepetido, boolean assinaturaRepetida,
                         boolean transmissaoRepetida, boolean processadoRepetido) {}
}
