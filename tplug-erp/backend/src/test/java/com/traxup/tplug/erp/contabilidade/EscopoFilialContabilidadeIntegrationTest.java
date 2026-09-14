package com.traxup.tplug.erp.contabilidade;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class EscopoFilialContabilidadeIntegrationTest {
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void administradorVeTodasEUsuarioComumSomenteFiliaisVinculadas() {
        UUID tenantId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID filialPermitida = UUID.randomUUID();
        UUID filialNegada = UUID.randomUUID();
        UUID administradorId = UUID.randomUUID();
        UUID contadorId = UUID.randomUUID();

        jdbc.update("INSERT INTO tenants (id, nome) VALUES (?, ?)",
                tenantId, "Tenant escopo contabil");
        jdbc.update("""
                INSERT INTO empresas (id, tenant_id, razao_social)
                VALUES (?, ?, ?)
                """, empresaId, tenantId, "Empresa escopo contabil");
        inserirFilial(filialPermitida, tenantId, empresaId,
                "Filial permitida");
        inserirFilial(filialNegada, tenantId, empresaId,
                "Filial negada");
        inserirUsuario(administradorId, tenantId, "Administrador");
        inserirUsuario(contadorId, tenantId, "Contador");

        UUID perfilAdminId = jdbc.queryForObject("""
                SELECT id FROM perfis
                WHERE tenant_id = ? AND UPPER(nome) = 'ADMIN'
                """, UUID.class, tenantId);
        jdbc.update("""
                INSERT INTO usuario_perfis
                    (tenant_id, usuario_id, perfil_id)
                VALUES (?, ?, ?)
                """, tenantId, administradorId, perfilAdminId);
        jdbc.update("""
                INSERT INTO usuario_filiais
                    (tenant_id, usuario_id, filial_id)
                VALUES (?, ?, ?)
                """, tenantId, contadorId, filialPermitida);

        var policy = new EscopoFilialContabilidade(jdbc);

        assertThat(policy.resolver(
                tenantId, administradorId, filialNegada).acessoTotal())
                .isTrue();
        assertThat(policy.resolver(
                tenantId, contadorId, filialPermitida).acessoTotal())
                .isFalse();
        assertThat(policy.resolver(
                tenantId, contadorId, null).acessoTotal())
                .isFalse();
        assertThrows(AccessDeniedException.class,
                () -> policy.resolver(
                        tenantId, contadorId, filialNegada));
    }

    @Test
    void exigeIdentidadeAutenticada() {
        var policy = new EscopoFilialContabilidade(jdbc);

        assertThrows(AccessDeniedException.class,
                () -> policy.resolver(
                        UUID.randomUUID(), null, null));
    }

    private void inserirFilial(UUID id, UUID tenantId,
                               UUID empresaId, String nome) {
        jdbc.update("""
                INSERT INTO filiais (id, tenant_id, empresa_id, nome)
                VALUES (?, ?, ?, ?)
                """, id, tenantId, empresaId, nome);
    }

    private void inserirUsuario(UUID id, UUID tenantId, String nome) {
        jdbc.update("""
                INSERT INTO usuarios
                    (id, tenant_id, nome, email, senha_hash)
                VALUES (?, ?, ?, ?, 'hash-teste')
                """, id, tenantId, nome, id + "@teste.local");
    }
}
