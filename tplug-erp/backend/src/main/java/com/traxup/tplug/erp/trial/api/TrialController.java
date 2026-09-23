package com.traxup.tplug.erp.trial.api;

import com.traxup.tplug.erp.trial.TrialProvisioningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/trials")
public class TrialController {
    private final TrialProvisioningService service;

    private final com.traxup.tplug.erp.auth.AuthApplicationService auth;
    private final com.traxup.tplug.erp.trial.mail.TrialEmailQueue emails;
    public TrialController(TrialProvisioningService service,com.traxup.tplug.erp.trial.mail.TrialEmailQueue emails, com.traxup.tplug.erp.auth.AuthApplicationService auth) { this.service = service; this.emails=emails; this.auth=auth; }
    @PostMapping("/reenviar-ativacao")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void reenviar(@Valid @RequestBody TrialReenviarRequest request) { auth.resolverTenant(request.codigoEmpresa(), request.tenantId()).ifPresent(id -> emails.resend(id,request.email())); }


    @org.springframework.beans.factory.annotation.Autowired
    private com.traxup.tplug.erp.auth.AccessRecoveryQueue recovery;

    public record RecoveryRequest(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=20) String documento,
                                  @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email @jakarta.validation.constraints.Size(max=254) String email) {}
    @PostMapping("/recuperar-acesso")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void recuperar(@Valid @RequestBody RecoveryRequest request) {
        recovery.requestDocument(request.documento(), request.email());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrialCadastroResponse cadastrar(@Valid @RequestBody TrialCadastroRequest request) {
        try { return service.provisionar(request); }
        catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new com.traxup.tplug.erp.shared.exception.RecursoConflitanteException("Empresa já cadastrada");
        }
    }
}
