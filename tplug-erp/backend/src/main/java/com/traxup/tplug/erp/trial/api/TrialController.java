package com.traxup.tplug.erp.trial.api;

import com.traxup.tplug.erp.trial.TrialProvisioningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/trials")
public class TrialController {
    private final TrialProvisioningService service;

    public TrialController(TrialProvisioningService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrialCadastroResponse cadastrar(@Valid @RequestBody TrialCadastroRequest request) {
        return service.provisionar(request);
    }
}
