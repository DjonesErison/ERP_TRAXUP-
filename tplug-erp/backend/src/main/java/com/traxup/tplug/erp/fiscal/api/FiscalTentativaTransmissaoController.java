package com.traxup.tplug.erp.fiscal.api;
import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalTentativaTransmissaoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/fiscal/tentativas")
public class FiscalTentativaTransmissaoController {
 private final FiscalTentativaTransmissaoApplicationService service; private final TenantContext context;
 public FiscalTentativaTransmissaoController(FiscalTentativaTransmissaoApplicationService s,TenantContext c){service=s;context=c;}
 @PostMapping("/{tentativaId}/transmissao-homologacao-simulada")
 @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
 public FiscalTentativaTransmissaoApplicationService.Resultado transmitir(@PathVariable UUID tentativaId){
  return service.transmitir(context.tenantId(),context.usuarioIdOuNulo(),tentativaId);
 }
}
