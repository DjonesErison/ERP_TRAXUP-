package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FiscalPerfilContabilidadeIntegrationTest {
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void provisionaPerfilContabilidadeSomenteLeituraParaNovoTenant() {
        Tenant tenant = tenantRepository.saveAndFlush(
                new Tenant("Tenant Perfil Contabilidade"));

        UUID perfilId = jdbc.queryForObject("""
                SELECT id
                FROM perfis
                WHERE tenant_id = ?
                  AND nome = 'CONTABILIDADE'
                  AND ativo = TRUE
                """, UUID.class, tenant.getId());

        List<String> permissoes = jdbc.queryForList("""
                SELECT perm.chave
                FROM perfil_permissoes pp
                JOIN permissoes perm ON perm.id = pp.permissao_id
                WHERE pp.tenant_id = ?
                  AND pp.perfil_id = ?
                ORDER BY perm.chave
                """, String.class, tenant.getId(), perfilId);

        assertThat(permissoes)
                .containsExactly(
                        "CONTABILIDADE_LIVRO_CAIXA_LER",
                        "FISCAL_REPOSITORIO_CONTABILIDADE_LER")
                .doesNotContain("FISCAL_DOCUMENTO_EMITIR");
    }
}
