package com.traxup.tplug.erp.trial;

import com.traxup.tplug.erp.empresa.Empresa;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.usuario.Usuario;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

class TrialSaasTest {
    @Test
    void criaTrialComSeteDiasEOnboardingPendente() {
        Tenant tenant = new Tenant("Loja Demo");
        Empresa empresa = new Empresa(tenant, "Loja Demo LTDA", "Loja Demo", "12345678000199");
        Usuario admin = new Usuario(tenant, "Admin", "admin@demo.com", "hash");
        Instant inicio = Instant.parse("2026-09-17T12:00:00Z");
        TrialSaas trial = new TrialSaas(tenant, empresa, admin, "admin@demo.com", "12345678000199",
                "87999999999", "Varejo", 1, "2026-09", "idem-1", inicio);
        assertThat(trial.getExpiraEm()).isEqualTo(Instant.parse("2026-09-24T12:00:00Z"));
        assertThat(trial.getStatus()).isEqualTo("ATIVO");
        assertThat(trial.getOnboardingStatus()).isEqualTo("PENDENTE");
    }
}
