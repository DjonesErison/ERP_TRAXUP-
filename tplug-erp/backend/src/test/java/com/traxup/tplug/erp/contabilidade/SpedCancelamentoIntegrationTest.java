package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class SpedCancelamentoIntegrationTest {
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private SpedExportacaoApplicationService exportacaoService;

    @Autowired
    private SpedCancelamentoApplicationService cancelamentoService;

    @Test
    void cancelaSomenteSolicitacaoDoTenantAutenticado() {
        Tenant dono = tenantRepository.saveAndFlush(
                new Tenant("Tenant SPED Cancelamento"));
        Tenant outro = tenantRepository.saveAndFlush(
                new Tenant("Outro Tenant SPED Cancelamento"));
        var exportacao = exportacaoService.solicitar(
                dono.getId(), null, "EFD_ICMS_IPI",
                YearMonth.of(2026, 9));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> cancelamentoService.cancelar(
                        outro.getId(), null, exportacao.id()));

        var cancelada = cancelamentoService.cancelar(
                dono.getId(), null, exportacao.id());
        assertThat(cancelada.status()).isEqualTo("CANCELADO");

        assertThrows(RecursoNaoEncontradoException.class,
                () -> cancelamentoService.cancelar(
                        dono.getId(), null, exportacao.id()));

        var reaberta = exportacaoService.solicitar(
                dono.getId(), null, "EFD_ICMS_IPI",
                YearMonth.of(2026, 9));
        assertThat(reaberta.id()).isEqualTo(exportacao.id());
        assertThat(reaberta.status()).isEqualTo("PENDENTE");
    }
}
