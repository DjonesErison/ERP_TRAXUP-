package com.traxup.tplug.erp.pessoa.endereco.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.pessoa.endereco.PessoaEnderecoApplicationService;
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
@RequestMapping("/api/v1/pessoas/{pessoaId}/enderecos")
public class PessoaEnderecoController {

    private final PessoaEnderecoApplicationService service;
    private final TenantContext tenantContext;

    public PessoaEnderecoController(PessoaEnderecoApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PESSOA_ENDERECO_LER')")
    public List<PessoaEnderecoResponse> listar(@PathVariable UUID pessoaId) {
        return service.listar(tenantContext.tenantId(), pessoaId).stream().map(PessoaEnderecoResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PESSOA_ENDERECO_GERENCIAR')")
    public PessoaEnderecoResponse criar(@PathVariable UUID pessoaId, @Valid @RequestBody CriarPessoaEnderecoRequest request) {
        return PessoaEnderecoResponse.from(service.criar(
                tenantContext.tenantId(), pessoaId, request.tipo(), request.logradouro(), request.numero(),
                request.complemento(), request.bairro(), request.cidade(), request.uf(), request.cep(), request.principal()));
    }
}
