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
class SpedResumoIntegrationTest {
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private SpedExportacaoApplicationService exportacaoService;

    @Autowired
    private SpedResumoApplicationService resumoService;

    @Test
    void agregaSomenteExportacoesDoTenantEPeriodoInformados() {
        Tenant tenant = tenantRepository.saveAndFlush(
                new Tenant("Tenant SPED Resumo"));
        Tenant outro = tenantRepository.saveAndFlush(
                new Tenant("Outro Tenant SPED Resumo"));

        exportacaoService.solicitar(
                tenant.getId(), null, "EFD_ICMS_IPI",
                YearMonth.of(2026, 7));
        exportacaoService.solicitar(
                tenant.getId(), null, "EFD_ICMS_IPI",
                YearMonth.of(2026, 8));
        exportacaoService.solicitar(
                tenant.getId(), null, "EFD_CONTRIBUICOES",
                YearMonth.of(2026, 8));
        exportacaoService.solicitar(
                outro.getId(), null, "EFD_ICMS_IPI",
                YearMonth.of(2026, 8));

        var resumo = resumoService.resumir(
                tenant.getId(), null, "EFD_ICMS_IPI",
                YearMonth.of(2026, 8), YearMonth.of(2026, 8));

        assertThat(resumo.total()).isEqualTo(1);
        assertThat(resumo.pendentes()).isEqualTo(1);
        assertThat(resumo.processando()).isZero();
        assertThat(resumo.concluidos()).isZero();
        assertThat(resumo.falhas()).isZero();
        assertThat(resumo.cancelados()).isZero();
    }
}
