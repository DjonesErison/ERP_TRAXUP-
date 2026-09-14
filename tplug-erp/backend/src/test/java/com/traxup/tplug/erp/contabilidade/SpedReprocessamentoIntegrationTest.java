package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class SpedReprocessamentoIntegrationTest {
    @Autowired
    private SpedReprocessamentoApplicationService service;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void reiniciaSomenteExportacaoComFalhaDoTenantInformado() {
        Tenant dono = tenantRepository.saveAndFlush(
                new Tenant("Tenant SPED Reprocessamento"));
        Tenant outro = tenantRepository.saveAndFlush(
                new Tenant("Outro Tenant SPED Reprocessamento"));
        var contextoDono = SpedFilialTestFixture.criar(
                jdbc, dono.getId(), "Reprocessamento");
        var contextoOutro = SpedFilialTestFixture.criar(
                jdbc, outro.getId(), "Reprocessamento outro");
        UUID exportacaoId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO contabilidade_sped_exportacoes (
                    id, tenant_id, filial_id, tipo, competencia, status,
                    tentativas_processamento, erro_codigo
                )
                VALUES (?, ?, ?, 'EFD_ICMS_IPI', ?, 'FALHOU', 5, ?)
                """, exportacaoId, dono.getId(), contextoDono.filialId(),
                Date.valueOf(YearMonth.of(2026, 8).atDay(1)),
                "GERACAO_OU_ARQUIVAMENTO");

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.reprocessar(
                        outro.getId(), contextoOutro.usuarioId(), exportacaoId));

        var resultado = service.reprocessar(
                dono.getId(), contextoDono.usuarioId(), exportacaoId);

        assertEquals(exportacaoId, resultado.id());
        assertEquals("PENDENTE", resultado.status());
        assertEquals(0, jdbc.queryForObject("""
                SELECT tentativas_processamento
                FROM contabilidade_sped_exportacoes
                WHERE tenant_id = ? AND id = ?
                """, Integer.class, dono.getId(), exportacaoId));
    }
}
