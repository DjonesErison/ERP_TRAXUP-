package com.traxup.tplug.erp.crm.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.crm.ClienteInteracaoApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/interacoes")
public class ClienteInteracaoController {
    private final ClienteInteracaoApplicationService service;
    private final TenantContext tenantContext;

    public ClienteInteracaoController(ClienteInteracaoApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CRM_CLIENTE_RETORNO_LER')")
    public List<ClienteInteracaoResponse> listar(
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) String canal,
            @RequestParam(required = false) String resultado,
            @RequestParam(required = false) Instant inicio,
            @RequestParam(required = false) Instant fim,
            @RequestParam(required = false, defaultValue = "100") Integer limite) {
        return service.listar(tenantContext.tenantId(), filialId, clienteId, canal, resultado, inicio, fim, limite)
                .stream().map(ClienteInteracaoResponse::from).toList();
    }

    @GetMapping("/{interacaoId}")
    @PreAuthorize("hasAuthority('CRM_CLIENTE_RETORNO_LER')")
    public ClienteInteracaoResponse buscar(@PathVariable UUID interacaoId) {
        return ClienteInteracaoResponse.from(service.buscar(tenantContext.tenantId(), interacaoId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CRM_CLIENTE_RETORNO_EDITAR')")
    public ClienteInteracaoResponse registrar(@Valid @RequestBody CriarClienteInteracaoRequest request) {
        return ClienteInteracaoResponse.from(service.registrar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), request.filialId(), request.clienteId(),
                request.followUpId(), request.canal(), request.resultado(), request.assunto(), request.ocorridoEm()));
    }
}
