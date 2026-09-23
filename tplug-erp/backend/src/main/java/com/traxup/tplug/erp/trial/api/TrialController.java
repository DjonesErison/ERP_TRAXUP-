package com.traxup.tplug.erp.trial.api;

import com.traxup.tplug.erp.trial.TrialApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/trials")
public class TrialController {
  private final TrialApplicationService service;
  public TrialController(TrialApplicationService service){this.service=service;}
  @PostMapping
  public ResponseEntity<TrialResponse> criar(@Valid @RequestBody CriarTrialRequest request){
    return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
  }
}
