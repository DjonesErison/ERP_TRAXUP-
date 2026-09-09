package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class FiscalRegraOperacaoApplicationService {
    private static final Pattern UF = Pattern.compile("[A-Z]{2}");
    private static final Pattern CFOP = Pattern.compile("[0-9]{4}");
    private static final Pattern CODIGO = Pattern.compile("[0-9]{3}");

    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;

    public FiscalRegraOperacaoApplicationService(JdbcTemplate jdbc, AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    @Transactional
    public Regra salvar(UUID tenantId, UUID usuarioId, Comando comando) {
        String operacao = normalizar(comando.tipoOperacao());
        String modelo = normalizar(comando.modelo()).replace("-", "");
        String regime = normalizar(comando.regimeTributario());
        String uf = normalizar(comando.ufDestino());
        String cfop = texto(comando.cfop());
        String cst = opcional(comando.cstIcms());
        String csosn = opcional(comando.csosn());
        validar(operacao, modelo, regime, uf, cfop, cst, csosn);

        UUID novoId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO fiscal_regras_operacao
                    (id, tenant_id, tipo_operacao, modelo, regime_tributario,
                     uf_destino, cfop, cst_icms, csosn, ativo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)
                ON CONFLICT (tenant_id, tipo_operacao, modelo, regime_tributario, uf_destino)
                DO UPDATE SET cfop = EXCLUDED.cfop, cst_icms = EXCLUDED.cst_icms,
                              csosn = EXCLUDED.csosn, ativo = TRUE,
                              atualizado_em = CURRENT_TIMESTAMP
                """, novoId, tenantId, operacao, modelo, regime, uf, cfop, cst, csosn);
        Regra regra = resolver(tenantId, operacao, modelo, regime, uf);
        auditoria.registrar(tenantId, usuarioId, null, null,
                "SALVAR_REGRA_OPERACAO", "FISCAL_REGRA_OPERACAO", regra.id(),
                "operacao=" + operacao + ";modelo=" + modelo + ";regime=" + regime
                        + ";uf=" + uf + ";cfop=" + cfop);
        return regra;
    }

    public Regra resolver(UUID tenantId, String tipoOperacao, String modelo,
                          String regimeTributario, String ufDestino) {
        var regras = jdbc.query("""
                SELECT id, tipo_operacao, modelo, regime_tributario, uf_destino,
                       cfop, cst_icms, csosn
                FROM fiscal_regras_operacao
                WHERE tenant_id = ? AND tipo_operacao = ? AND modelo = ?
                  AND regime_tributario = ? AND uf_destino = ? AND ativo
                """, (rs, n) -> new Regra(rs.getObject("id", UUID.class),
                        rs.getString("tipo_operacao"), rs.getString("modelo"),
                        rs.getString("regime_tributario"), rs.getString("uf_destino"),
                        rs.getString("cfop"), rs.getString("cst_icms"), rs.getString("csosn")),
                tenantId, normalizar(tipoOperacao), normalizar(modelo).replace("-", ""),
                normalizar(regimeTributario), normalizar(ufDestino));
        if (regras.isEmpty()) {
            throw new RecursoNaoEncontradoException("Regra fiscal nao encontrada para o contexto informado");
        }
        return regras.getFirst();
    }

    private void validar(String operacao, String modelo, String regime, String uf,
                         String cfop, String cst, String csosn) {
        if (!operacao.equals("VENDA") && !operacao.equals("DEVOLUCAO"))
            throw new IllegalArgumentException("Tipo de operacao fiscal invalido");
        if (!modelo.equals("NFCE") && !modelo.equals("NFE"))
            throw new IllegalArgumentException("Modelo fiscal invalido");
        if (!UF.matcher(uf).matches()) throw new IllegalArgumentException("UF de destino invalida");
        if (!CFOP.matcher(cfop).matches()) throw new IllegalArgumentException("CFOP invalido");
        if (regime.equals("SIMPLES_NACIONAL")) {
            if (csosn == null || !CODIGO.matcher(csosn).matches() || cst != null)
                throw new IllegalArgumentException("Simples Nacional exige apenas CSOSN com 3 digitos");
        } else if (regime.equals("REGIME_NORMAL")) {
            if (cst == null || !CODIGO.matcher(cst).matches() || csosn != null)
                throw new IllegalArgumentException("Regime Normal exige apenas CST ICMS com 3 digitos");
        } else throw new IllegalArgumentException("Regime tributario invalido");
    }

    private String normalizar(String valor) {
        return texto(valor).toUpperCase(Locale.ROOT).replace(' ', '_');
    }
    private String texto(String valor) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Campo fiscal obrigatorio");
        return valor.trim();
    }
    private String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public record Comando(String tipoOperacao, String modelo, String regimeTributario,
                          String ufDestino, String cfop, String cstIcms, String csosn) {}
    public record Regra(UUID id, String tipoOperacao, String modelo, String regimeTributario,
                        String ufDestino, String cfop, String cstIcms, String csosn) {}
}
