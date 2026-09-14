package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.YearMonth;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class SpedFilialAcessoIntegrationTest {
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private SpedExportacaoApplicationService exportacaoService;

    @Autowired
    private SpedResumoApplicationService resumoService;

    @Test
    void separaCompetenciaPorFilialEFiltraUsuarioNaoAdministrador() {
        Tenant tenant = tenantRepository.saveAndFlush(
                new Tenant("Tenant SPED duas filiais"));
        var admin = SpedFilialTestFixture.criar(
                jdbc, tenant.getId(), "Matriz");
        UUID filialB = criarSegundaFilial(tenant.getId());
        UUID contadorId = criarContador(
                tenant.getId(), admin.filialId());

        YearMonth competencia = YearMonth.of(2026, 9);
        var matriz = exportacaoService.solicitar(
                tenant.getId(), admin.usuarioId(), admin.filialId(),
                "EFD_ICMS_IPI", competencia);
        var filial = exportacaoService.solicitar(
                tenant.getId(), admin.usuarioId(), filialB,
                "EFD_ICMS_IPI", competencia);

        assertThat(filial.id()).isNotEqualTo(matriz.id());
        assertThat(exportacaoService.listar(
                tenant.getId(), contadorId, null,
                "EFD_ICMS_IPI", null, null, null, 100))
                .extracting(SpedExportacaoApplicationService.Exportacao::id)
                .containsExactly(matriz.id());
        assertThat(resumoService.resumir(
                tenant.getId(), contadorId, null,
                "EFD_ICMS_IPI", competencia, competencia).total())
                .isEqualTo(1);
        assertThrows(AccessDeniedException.class,
                () -> exportacaoService.solicitar(
                        tenant.getId(), contadorId, filialB,
                        "EFD_CONTRIBUICOES", competencia));
    }

    @Test
    void bancoExigeFilialOuMarcacaoExplicitaDeLegado() {
        Tenant tenant = tenantRepository.saveAndFlush(
                new Tenant("Tenant SPED filial obrigatoria"));

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("""
                        INSERT INTO contabilidade_sped_exportacoes (
                            id, tenant_id, tipo, competencia, status
                        )
                        VALUES (?, ?, 'EFD_ICMS_IPI', ?, 'PENDENTE')
                        """, UUID.randomUUID(), tenant.getId(),
                        Date.valueOf(YearMonth.of(2026, 9).atDay(1))));
    }

    private UUID criarSegundaFilial(UUID tenantId) {
        UUID empresaId = jdbc.queryForObject("""
                SELECT empresa_id FROM filiais
                WHERE tenant_id = ?
                ORDER BY id
                LIMIT 1
                """, UUID.class, tenantId);
        UUID filialId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO filiais (id, tenant_id, empresa_id, nome)
                VALUES (?, ?, ?, 'Filial SPED B')
                """, filialId, tenantId, empresaId);
        return filialId;
    }

    private UUID criarContador(UUID tenantId, UUID filialId) {
        UUID usuarioId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO usuarios
                    (id, tenant_id, nome, email, senha_hash)
                VALUES (?, ?, 'Contador SPED', ?, 'hash-teste')
                """, usuarioId, tenantId, usuarioId + "@teste.local");
        jdbc.update("""
                INSERT INTO usuario_filiais
                    (tenant_id, usuario_id, filial_id)
                VALUES (?, ?, ?)
                """, tenantId, usuarioId, filialId);
        return usuarioId;
    }
}
