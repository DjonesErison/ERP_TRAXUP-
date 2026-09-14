package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.contabilidade.api.FilialContabilidadeController;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class FilialContabilidadeIntegrationTest {
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private FilialContabilidadeApplicationService service;

    @Test
    void listaTodasParaAdminESomenteVinculadasParaContador() {
        Tenant tenant = tenantRepository.saveAndFlush(
                new Tenant("Tenant filiais contabilidade"));
        var admin = SpedFilialTestFixture.criar(
                jdbc, tenant.getId(), "Filiais");
        UUID segundaFilialId = criarSegundaFilial(tenant.getId());
        UUID contadorId = criarContador(
                tenant.getId(), admin.filialId());

        assertThat(service.listar(tenant.getId(), admin.usuarioId()))
                .extracting(
                        FilialContabilidadeApplicationService
                                .FilialDisponivel::id)
                .containsExactlyInAnyOrder(
                        admin.filialId(), segundaFilialId);
        assertThat(service.listar(tenant.getId(), contadorId))
                .extracting(
                        FilialContabilidadeApplicationService
                                .FilialDisponivel::id)
                .containsExactly(admin.filialId());
    }

    @Test
    void endpointExigePermissaoDedicada() throws NoSuchMethodException {
        PreAuthorize regra = FilialContabilidadeController.class
                .getDeclaredMethod("listar")
                .getAnnotation(PreAuthorize.class);

        assertEquals("hasAuthority('CONTABILIDADE_FILIAIS_LER')",
                regra.value());
    }

    private UUID criarSegundaFilial(UUID tenantId) {
        UUID empresaId = jdbc.queryForObject("""
                SELECT empresa_id
                FROM filiais
                WHERE tenant_id = ?
                ORDER BY id
                LIMIT 1
                """, UUID.class, tenantId);
        UUID filialId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO filiais (id, tenant_id, empresa_id, nome)
                VALUES (?, ?, ?, 'Filial Contabilidade B')
                """, filialId, tenantId, empresaId);
        return filialId;
    }

    private UUID criarContador(UUID tenantId, UUID filialId) {
        UUID usuarioId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO usuarios
                    (id, tenant_id, nome, email, senha_hash)
                VALUES (?, ?, 'Contador de teste', ?, 'hash-teste')
                """, usuarioId, tenantId, usuarioId + "@teste.local");
        jdbc.update("""
                INSERT INTO usuario_filiais
                    (tenant_id, usuario_id, filial_id)
                VALUES (?, ?, ?)
                """, tenantId, usuarioId, filialId);
        return usuarioId;
    }
}
