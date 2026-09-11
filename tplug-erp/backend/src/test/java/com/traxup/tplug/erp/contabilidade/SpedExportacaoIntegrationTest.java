package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SpedExportacaoIntegrationTest {
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private SpedExportacaoApplicationService service;

    @Test
    void reutilizaSolicitacaoNoTenantSemCruzarEmpresas() {
        Tenant tenantA = tenantRepository.saveAndFlush(
                new Tenant("Tenant SPED A"));
        Tenant tenantB = tenantRepository.saveAndFlush(
                new Tenant("Tenant SPED B"));
        YearMonth competencia = YearMonth.of(2026, 9);

        var primeira = service.solicitar(
                tenantA.getId(), null, "EFD_ICMS_IPI", competencia);
        var repetida = service.solicitar(
                tenantA.getId(), null, "EFD_ICMS_IPI", competencia);
        var outroTenant = service.solicitar(
                tenantB.getId(), null, "EFD_ICMS_IPI", competencia);

        assertThat(repetida.id()).isEqualTo(primeira.id());
        assertThat(outroTenant.id()).isNotEqualTo(primeira.id());
        assertThat(service.listar(tenantA.getId(), null, null, 100))
                .extracting(SpedExportacaoApplicationService.Exportacao::id)
                .containsExactly(primeira.id());
    }
}
