package com.traxup.tplug.erp.fiscal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FiscalPainelOperacionalApplicationService {
    private final JdbcTemplate jdbc;

    public FiscalPainelOperacionalApplicationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Painel buscar(UUID tenantId) {
        return jdbc.queryForObject("""
                SELECT
                  (SELECT count(*) FROM fiscal_tentativas_emissao
                    WHERE tenant_id = ?) AS tentativas_total,
                  (SELECT count(*) FROM fiscal_tentativas_emissao
                    WHERE tenant_id = ? AND status = 'CRIADA') AS tentativas_criadas,
                  (SELECT count(*) FROM fiscal_tentativas_emissao
                    WHERE tenant_id = ? AND status = 'EM_PROCESSAMENTO') AS tentativas_processando,
                  (SELECT count(*) FROM fiscal_tentativas_emissao
                    WHERE tenant_id = ? AND status = 'CONCLUIDA') AS tentativas_concluidas,
                  (SELECT count(*) FROM fiscal_tentativas_emissao
                    WHERE tenant_id = ? AND status = 'FALHOU') AS tentativas_falhas,
                  (SELECT count(*) FROM fiscal_tentativa_falhas
                    WHERE tenant_id = ? AND resolvida_em IS NULL) AS falhas_pendentes,
                  (SELECT count(*) FROM fiscal_tentativa_falhas
                    WHERE tenant_id = ? AND resolvida_em IS NULL
                      AND etapa = 'XML') AS falhas_xml,
                  (SELECT count(*) FROM fiscal_tentativa_falhas
                    WHERE tenant_id = ? AND resolvida_em IS NULL
                      AND etapa = 'ASSINATURA') AS falhas_assinatura,
                  (SELECT count(*) FROM fiscal_tentativa_falhas
                    WHERE tenant_id = ? AND resolvida_em IS NULL
                      AND etapa = 'TRANSMISSAO') AS falhas_transmissao,
                  (SELECT count(*) FROM fiscal_tentativa_falhas
                    WHERE tenant_id = ? AND resolvida_em IS NULL
                      AND etapa = 'PROCESSADO') AS falhas_processado,
                  (SELECT count(*) FROM fiscal_rejeicoes
                    WHERE tenant_id = ? AND status = 'ABERTA') AS rejeicoes_abertas,
                  (SELECT count(*) FROM fiscal_rejeicoes
                    WHERE tenant_id = ? AND status = 'ABERTA'
                      AND corrigivel) AS rejeicoes_corrigiveis
                """, (rs, n) -> {
                    long falhas = rs.getLong("falhas_pendentes");
                    long rejeicoes = rs.getLong("rejeicoes_abertas");
                    long processando = rs.getLong("tentativas_processando");
                    return new Painel(Instant.now(),
                            saude(falhas, rejeicoes, processando),
                            new Tentativas(rs.getLong("tentativas_total"),
                                    rs.getLong("tentativas_criadas"), processando,
                                    rs.getLong("tentativas_concluidas"),
                                    rs.getLong("tentativas_falhas")),
                            new Falhas(falhas, rs.getLong("falhas_xml"),
                                    rs.getLong("falhas_assinatura"),
                                    rs.getLong("falhas_transmissao"),
                                    rs.getLong("falhas_processado")),
                            new Rejeicoes(rejeicoes,
                                    rs.getLong("rejeicoes_corrigiveis")));
                }, tenantId, tenantId, tenantId, tenantId, tenantId, tenantId,
                tenantId, tenantId, tenantId, tenantId, tenantId, tenantId);
    }

    static String saude(long falhasPendentes, long rejeicoesAbertas,
                        long tentativasProcessando) {
        if (falhasPendentes > 0) return "CRITICO";
        if (rejeicoesAbertas > 0 || tentativasProcessando > 0) return "ATENCAO";
        return "NORMAL";
    }

    public record Painel(Instant consultadoEm, String saude,
                         Tentativas tentativas, Falhas falhas,
                         Rejeicoes rejeicoes) {}
    public record Tentativas(long total, long criadas, long processando,
                             long concluidas, long falharam) {}
    public record Falhas(long pendentes, long xml, long assinatura,
                         long transmissao, long processado) {}
    public record Rejeicoes(long abertas, long corrigiveis) {}
}
