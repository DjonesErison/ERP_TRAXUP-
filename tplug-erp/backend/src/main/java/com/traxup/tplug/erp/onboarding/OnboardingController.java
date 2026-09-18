package com.traxup.tplug.erp.onboarding;
import jakarta.validation.Valid; import jakarta.validation.constraints.Size; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/onboarding")
public class OnboardingController{
 private final OnboardingService service; public OnboardingController(OnboardingService service){this.service=service;}
 @GetMapping public OnboardingService.Status status(){return service.status();}
 @PostMapping("/concluir") public OnboardingService.Status concluir(@Valid @RequestBody ConcluirRequest r){return service.concluir(r.nomeFilial(),r.cnpj());}
 public record ConcluirRequest(@Size(max=200) String nomeFilial,@Size(max=18) String cnpj){}
}
