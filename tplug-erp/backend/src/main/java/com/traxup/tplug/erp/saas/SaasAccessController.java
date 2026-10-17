package com.traxup.tplug.erp.saas;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
/** Consulta apenas a elegibilidade do próprio usuário autenticado. */
@RestController
public class SaasAccessController {
 private final SaasAccess access;
 public SaasAccessController(SaasAccess access){this.access=access;}
 @GetMapping("/api/v1/saas/acesso") public Map<String,Boolean> acesso(){return Map.of("permitido",access.permitido());}
}
