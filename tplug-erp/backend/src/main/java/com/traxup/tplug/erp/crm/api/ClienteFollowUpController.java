package com.traxup.tplug.erp.crm.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.crm.ClienteFollowUpApplicationService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/followups")
public class ClienteFollowUpController {
    private final ClienteFollowUpApplicationService service;
    private final TenantContext tenantContext;

    public ClienteFollowUpController(ClienteFollowUpApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CRM_CLIENTE_RETORNO_LER')")
    public List<ClienteFollowUpResponse> listar(
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false, defaultValue = "PENDENTE") String status,
            @RequestParam(required = false, defaultValue = "100") Integer limite) {
        return service.listar(tenantContext.tenantId(), filialId, clienteId, status, limite)
                .stream().map(ClienteFollowUpResponse::from).toList();
    }

    @GetMapping("/{followUpId}")
    @PreAuthorize("hasAuthority('CRM_CLIENTE_RETORNO_LER')")
    public ClienteFollowUpResponse buscar(@PathVariable UUID followUpId) {
        return ClienteFollowUpResponse.from(service.buscar(tenantContext.tenantId(), followUpId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CRM_CLIENTE_RETORNO_EDITAR')")
    public ClienteFollowUpResponse criar(@Valid @RequestBody CriarClienteFollowUpRequest request) {
        return ClienteFollowUpResponse.from(service.criar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), request.filialId(), request.clienteId(),
                request.assunto(), request.observacao(), request.agendadoPara()));
    }

    @PostMapping("/{followUpId}/concluir")
    @PreAuthorize("hasAuthority('CRM_CLIENTE_RETORNO_EDITAR')")
    public ClienteFollowUpResponse concluir(@PathVariable UUID followUpId) {
        return ClienteFollowUpResponse.from(service.concluir(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), followUpId));
    }

    @PostMapping("/{followUpId}/cancelar")
    @PreAuthorize("hasAuthority('CRM_CLIENTE_RETORNO_EDITAR')")
    public ClienteFollowUpResponse cancelar(@PathVariable UUID followUpId) {
        return ClienteFollowUpResponse.from(service.cancelar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), followUpId));
    }
}
