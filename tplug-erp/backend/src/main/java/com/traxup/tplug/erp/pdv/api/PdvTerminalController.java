package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.pdv.PdvTerminalApplicationService;
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
@RequestMapping("/api/v1/pdv/terminais")
public class PdvTerminalController {
    private final PdvTerminalApplicationService service;
    private final TenantContext tenantContext;

    public PdvTerminalController(PdvTerminalApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PDV_TERMINAL_LER')")
    public List<PdvTerminalResponse> listar(@RequestParam(required = false) UUID filialId) {
        return service.listar(tenantContext.tenantId(), filialId).stream().map(PdvTerminalResponse::from).toList();
    }

    @GetMapping("/{terminalId}")
    @PreAuthorize("hasAuthority('PDV_TERMINAL_LER')")
    public PdvTerminalResponse buscar(@PathVariable UUID terminalId) {
        return PdvTerminalResponse.from(service.buscar(tenantContext.tenantId(), terminalId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PDV_TERMINAL_GERENCIAR')")
    public PdvTerminalResponse criar(@Valid @RequestBody CriarPdvTerminalRequest request) {
        return PdvTerminalResponse.from(service.criar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), request.filialId(),
                request.codigo(), request.nome(), request.serie()));
    }

    @PostMapping("/{terminalId}/ativar")
    @PreAuthorize("hasAuthority('PDV_TERMINAL_GERENCIAR')")
    public PdvTerminalResponse ativar(@PathVariable UUID terminalId) {
        return PdvTerminalResponse.from(service.ativar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), terminalId));
    }

    @PostMapping("/{terminalId}/desativar")
    @PreAuthorize("hasAuthority('PDV_TERMINAL_GERENCIAR')")
    public PdvTerminalResponse desativar(@PathVariable UUID terminalId) {
        return PdvTerminalResponse.from(service.desativar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), terminalId));
    }
}
