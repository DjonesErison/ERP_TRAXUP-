package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SpedExportacaoIntegrationTest {
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private SpedExportacaoApplicationService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void reutilizaSolicitacaoNoTenantSemCruzarEmpresas() {
        Tenant tenantA = tenantRepository.saveAndFlush(
                new Tenant("Tenant SPED A"));
        Tenant tenantB = tenantRepository.saveAndFlush(
                new Tenant("Tenant SPED B"));
        var contextoA = SpedFilialTestFixture.criar(
                jdbc, tenantA.getId(), "A");
        var contextoB = SpedFilialTestFixture.criar(
                jdbc, tenantB.getId(), "B");
        YearMonth competencia = YearMonth.of(2026, 9);

        var primeira = service.solicitar(
                tenantA.getId(), contextoA.usuarioId(),
                contextoA.filialId(), "EFD_ICMS_IPI", competencia);
        var repetida = service.solicitar(
                tenantA.getId(), contextoA.usuarioId(),
                contextoA.filialId(), "EFD_ICMS_IPI", competencia);
        var outroTenant = service.solicitar(
                tenantB.getId(), contextoB.usuarioId(),
                contextoB.filialId(), "EFD_ICMS_IPI", competencia);

        assertThat(repetida.id()).isEqualTo(primeira.id());
        assertThat(outroTenant.id()).isNotEqualTo(primeira.id());
        assertThat(service.listar(
                tenantA.getId(), contextoA.usuarioId(), null, 100))
                .extracting(SpedExportacaoApplicationService.Exportacao::id)
                .containsExactly(primeira.id());
    }

    @Test
    void combinaFiltrosOpcionaisDeStatusTipoECompetencia() {
        Tenant tenant = tenantRepository.saveAndFlush(
                new Tenant("Tenant SPED Filtros"));
        var contexto = SpedFilialTestFixture.criar(
                jdbc, tenant.getId(), "Filtros");
        var julho = service.solicitar(
                tenant.getId(), contexto.usuarioId(), contexto.filialId(),
                "EFD_ICMS_IPI",
                YearMonth.of(2026, 7));
        var agosto = service.solicitar(
                tenant.getId(), contexto.usuarioId(), contexto.filialId(),
                "EFD_ICMS_IPI",
                YearMonth.of(2026, 8));
        service.solicitar(
                tenant.getId(), contexto.usuarioId(), contexto.filialId(),
                "EFD_CONTRIBUICOES",
                YearMonth.of(2026, 8));

        assertThat(service.listar(
                tenant.getId(), contexto.usuarioId(), contexto.filialId(),
                "EFD_ICMS_IPI", "PENDENTE",
                YearMonth.of(2026, 7), YearMonth.of(2026, 8), 100))
                .extracting(SpedExportacaoApplicationService.Exportacao::id)
                .containsExactly(agosto.id(), julho.id());
        assertThat(service.listar(
                tenant.getId(), contexto.usuarioId(), contexto.filialId(),
                null, "CONCLUIDO", null, null, 100))
                .isEmpty();
    }
    @Test
    void protegeArquivoConcluidoPorNoMinimoCincoAnos() {
        Tenant tenant = tenantRepository.saveAndFlush(
                new Tenant("Tenant SPED Retencao"));
        var contexto = SpedFilialTestFixture.criar(
                jdbc, tenant.getId(), "Retencao");
        var exportacao = service.solicitar(
                tenant.getId(), contexto.usuarioId(), contexto.filialId(),
                "EFD_ICMS_IPI",
                YearMonth.of(2026, 9));

        jdbc.update("""
                UPDATE contabilidade_sped_exportacoes
                SET status = 'CONCLUIDO',
                    chave_objeto = 'tenants/teste/sped.txt',
                    hash_sha256 = repeat('a', 64),
                    versao_layout = '019',
                    concluido_em = CURRENT_TIMESTAMP,
                    retencao_ate = CURRENT_TIMESTAMP + INTERVAL '5 years',
                    atualizado_em = CURRENT_TIMESTAMP
                WHERE tenant_id = ? AND id = ?
                """, tenant.getId(), exportacao.id());

        var concluida = service.listar(
                tenant.getId(), contexto.usuarioId(), contexto.filialId(),
                null, "CONCLUIDO", null, null, 10).getFirst();
        assertThat(concluida.retencaoAte()).isAfter(
                Instant.now().plus(4 * 365L, ChronoUnit.DAYS));

        assertThatThrownBy(() -> jdbc.update("""
                DELETE FROM contabilidade_sped_exportacoes
                WHERE tenant_id = ? AND id = ?
                """, tenant.getId(), exportacao.id()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

}
