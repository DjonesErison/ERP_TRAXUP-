package com.traxup.tplug.erp.fiscal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FiscalTentativaTransmissaoApplicationServiceTest {
 @Test void exigeEstadoAmbienteETipoCorretos(){
  assertDoesNotThrow(()->FiscalTentativaTransmissaoApplicationService.validar("EM_PROCESSAMENTO","HOMOLOGACAO","SIMULADA"));
  assertThrows(IllegalArgumentException.class,()->FiscalTentativaTransmissaoApplicationService.validar("CRIADA","HOMOLOGACAO","SIMULADA"));
  assertThrows(IllegalArgumentException.class,()->FiscalTentativaTransmissaoApplicationService.validar("EM_PROCESSAMENTO","PRODUCAO","SIMULADA"));
  assertThrows(IllegalArgumentException.class,()->FiscalTentativaTransmissaoApplicationService.validar("EM_PROCESSAMENTO","HOMOLOGACAO","REAL"));
 }
}
