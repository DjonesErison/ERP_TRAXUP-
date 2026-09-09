package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class FiscalDocumentoRegraApplicationService {
    private final JdbcTemplate jdbc;
    private final FiscalRegraOperacaoApplicationService regras;
    private final AuditoriaApplicationService auditoria;

    public FiscalDocumentoRegraApplicationService(JdbcTemplate jdbc,
                                                  FiscalRegraOperacaoApplicationService regras,
                                                  AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.regras = regras;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado aplicar(UUID tenantId, UUID usuarioId, UUID documentoId,
                             String tipoOperacao, String regimeTributario, String ufDestino) {
        Documento documento = buscarDocumento(tenantId, documentoId);
        var regra = regras.resolver(tenantId, tipoOperacao, documento.modelo(),
                regimeTributario, ufDestino);

        if (documento.regraId() != null) {
            validarReplay(documento, regra);
            return Resultado.from(documento, true);
        }

        int alterados = jdbc.update("""
                UPDATE fiscal_documentos
                SET regra_operacao_id = ?, tipo_operacao = ?, regime_tributario = ?,
                    uf_destino = ?, cfop = ?, cst_icms = ?, csosn = ?,
                    regra_aplicada_em = CURRENT_TIMESTAMP
                WHERE tenant_id = ? AND id = ? AND regra_operacao_id IS NULL
                """, regra.id(), regra.tipoOperacao(), regra.regimeTributario(),
                regra.ufDestino(), regra.cfop(), regra.cstIcms(), regra.csosn(),
                tenantId, documentoId);

        Documento aplicado = buscarDocumento(tenantId, documentoId);
        validarReplay(aplicado, regra);
        if (alterados == 1) {
            auditoria.registrar(tenantId, usuarioId, null, documento.filialId(),
                    "APLICAR_REGRA_OPERACAO", "FISCAL_DOCUMENTO", documentoId,
                    "regraId=" + regra.id() + ";cfop=" + regra.cfop()
                            + ";regime=" + regra.regimeTributario() + ";uf=" + regra.ufDestino());
        }
        return Resultado.from(aplicado, alterados == 0);
    }

    private Documento buscarDocumento(UUID tenantId, UUID documentoId) {
        return jdbc.query("""
                SELECT id, filial_id, modelo,, regra_operacao_id, tipo_operacao,
                       regime_tributario, uf_destino, cfop, cst_icms, csosn
                FROM fiscal_documentos WHERE tenant_id = ? AND id = ?
                """, (rs, n) -> new Documento(rs.getObject("id", UUID.class),
                        rs.getObject("filial_id", UUID.class), rs.getString("modelo"),
                        rs.getObject("regra_operacao_id", UUID.class), rs.getString("tipo_operacao"),
                        rs.getString("regime_tributario"), rs.getString("uf_destino"),
                        rs.getString("cfop"), rs.getString("cst_icms"), rs.getString("csosn")),
                tenantId, documentoId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Documento fiscal nao encontrado para o tenant informado"));
    }

    private void validarReplay(Documento d, FiscalRegraOperacaoApplicationService.Regra r) {
        if (!Objects.equals(d.regraId(), r.id()) || !Objects.equals(d.tipoOperacao(), r.tipoOperacao())
                || !Objects.equals(d.regime(), r.regimeTributario())
                || !Objects.equals(d.uf(), r.ufDestino()) || !Objects.equals(d.cfop(), r.cfop())
                || !Objects.equals(d.cst(), r.cstIcms()) || !Objects.equals(d.csosn(), r.csosn())) {
            throw new IllegalArgumentException("Documento ja possui regra fiscal diferente e imutavel");
        }
    }

    private record Documento(UUID id, UUID filialId, String modelo, UUID regraId,
                             String tipoOperacao, String regime, String uf,
                             String cfop, String cst, String csosn) {}

    public record Resultado(UUID documentoId, UUID regraId, String tipoOperacao,
                            String regimeTributario, String ufDestino, String cfop,
                            String cstIcms, String csosn, boolean repetida) {
        static Resultado from(Documento d, boolean repetida) {
            return new Resultado(d.id(), d.regraId(), d.tipoOperacao(), d.regime(),
                    d.uf(), d.cfop(), d.cst(), d.csosn(), repetida);
        }
    }
}
