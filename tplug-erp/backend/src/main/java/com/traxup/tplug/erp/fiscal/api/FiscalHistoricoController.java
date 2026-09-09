package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalHistoricoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/documentos")
public class FiscalHistoricoController {
    private final FiscalHistoricoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalHistoricoController(FiscalHistoricoApplicationService service,
                                     TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{documentoId}/historico")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public Response buscar(@PathVariable UUID documentoId) {
        var h = service.buscar(tenantContext.tenantId(), documentoId);
        return new Response(h.documentoId(), h.solicitacaoId(), h.filialId(),
                h.pedidoVendaId(), h.modelo(), h.ambiente(), h.solicitacaoStatus(),
                h.etapaAtual(), h.serie(), h.numero(), h.solicitadoEm(),
                h.solicitacaoAtualizadoEm(), h.estruturadoEm(),
                new Xml(h.xmlId(), h.xmlVersao(), h.xmlHashSha256(), h.xmlGeradoEm()),
                new Assinatura(h.assinaturaId(), h.assinaturaTipo(),
                        h.assinaturaAlgoritmo(), h.assinaturaHashSha256(), h.assinadoEm()),
                new Transmissao(h.transmissaoId(), h.transmissaoProvedor(),
                        h.transmissaoStatus(), h.codigoResposta(), h.mensagemResposta(),
                        h.protocolo(), h.transmitidoEm()),
                new Processado(h.processadoId(), h.processadoTipo(), h.processadoVersao(),
                        h.processadoHashSha256(), h.processadoGeradoEm()));
    }

    public record Response(UUID documentoId, UUID solicitacaoId, UUID filialId,
                           UUID pedidoVendaId, String modelo, String ambiente,
                           String solicitacaoStatus, String etapaAtual, Integer serie,
                           Long numero, Instant solicitadoEm,
                           Instant solicitacaoAtualizadoEm, Instant estruturadoEm,
                           Xml xml, Assinatura assinatura, Transmissao transmissao,
                           Processado processado) {}

    public record Xml(UUID id, String versao, String hashSha256, Instant geradoEm) {}
    public record Assinatura(UUID id, String tipo, String algoritmo,
                             String hashSha256, Instant assinadoEm) {}
    public record Transmissao(UUID id, String provedor, String status,
                              String codigo, String mensagem, String protocolo,
                              Instant transmitidoEm) {}
    public record Processado(UUID id, String tipo, String versao,
                             String hashSha256, Instant geradoEm) {}
}
