package com.traxup.tplug.erp.pessoa.contato.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.pessoa.contato.PessoaContatoApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pessoas/{pessoaId}/contatos")
public class PessoaContatoController {

    private final PessoaContatoApplicationService service;
    private final TenantContext tenantContext;

    public PessoaContatoController(PessoaContatoApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PESSOA_CONTATO_LER')")
    public List<PessoaContatoResponse> listar(@PathVariable UUID pessoaId) {
        return service.listar(tenantContext.tenantId(), pessoaId).stream().map(PessoaContatoResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PESSOA_CONTATO_GERENCIAR')")
    public PessoaContatoResponse criar(@PathVariable UUID pessoaId, @Valid @RequestBody CriarPessoaContatoRequest request) {
        return PessoaContatoResponse.from(service.criar(tenantContext.tenantId(), pessoaId, request.nome(), request.cargo(),
                request.email(), request.telefone(), request.principal()));
    }
}
