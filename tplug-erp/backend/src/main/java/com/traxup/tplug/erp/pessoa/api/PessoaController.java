package com.traxup.tplug.erp.pessoa.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.pessoa.PessoaApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pessoas")
public class PessoaController {

    private final PessoaApplicationService service;
    private final TenantContext tenantContext;

    public PessoaController(PessoaApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PESSOA_LER')")
    public List<PessoaResponse> listar(@RequestParam(required = false) String papel) {
        return service.listar(tenantContext.tenantId(), papel).stream().map(PessoaResponse::from).toList();
    }

    @GetMapping("/{pessoaId}")
    @PreAuthorize("hasAuthority('PESSOA_LER')")
    public PessoaResponse buscar(@PathVariable UUID pessoaId) {
        return PessoaResponse.from(service.buscarPorId(tenantContext.tenantId(), pessoaId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PESSOA_CRIAR')")
    public PessoaResponse criar(@Valid @RequestBody CriarPessoaRequest request) {
        return PessoaResponse.from(service.criar(
                tenantContext.tenantId(), request.tipoPessoa(), request.nomeRazaoSocial(), request.nomeFantasia(),
                request.cpfCnpj(), request.email(), request.telefone(), request.cliente(), request.fornecedor()));
    }

    @PatchMapping("/{pessoaId}/desativar")
    @PreAuthorize("hasAuthority('PESSOA_DESATIVAR')")
    public PessoaResponse desativar(@PathVariable UUID pessoaId) {
        return PessoaResponse.from(service.desativar(tenantContext.tenantId(), pessoaId));
    }
}
