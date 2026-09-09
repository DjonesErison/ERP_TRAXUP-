package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

@Service
public class FiscalCertificadoApplicationService {
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;

    public FiscalCertificadoApplicationService(JdbcTemplate jdbc,
                                               AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    public Resultado buscarAtivo(UUID tenantId, UUID filialId) {
        return buscarRegistroAtivo(tenantId, filialId)
                .map(registro -> registro.resultado(false))
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Certificado fiscal ativo nao encontrado para a filial"));
    }

    @Transactional
    public Resultado salvar(UUID tenantId, UUID usuarioId, UUID filialId,
                            String tipo, String titular, String documentoTitular,
                            String numeroSerie, String thumbprintSha256,
                            OffsetDateTime validadeInicio, OffsetDateTime validadeFim,
                            String cofreSegredos, String referenciaSegredo) {
        UUID empresaId = jdbc.query("""
                SELECT empresa_id FROM filiais
                WHERE tenant_id = ? AND id = ? AND ativo = TRUE
                FOR UPDATE
                """, (rs, n) -> rs.getObject("empresa_id", UUID.class),
                tenantId, filialId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Filial ativa nao encontrada para o tenant informado"));

        String tipoNormalizado = normalizarTipo(tipo);
        String titularNormalizado = obrigatorio(titular, "Titular obrigatorio", 255);
        String documentoNormalizado = somenteDigitos(documentoTitular);
        String serieNormalizada = obrigatorio(numeroSerie, "Numero de serie obrigatorio", 120);
        String thumbprintNormalizado = normalizarThumbprint(thumbprintSha256);
        String cofreNormalizado = obrigatorio(cofreSegredos, "Cofre de segredos obrigatorio", 60);
        String referenciaNormalizada = obrigatorio(
                referenciaSegredo, "Referencia do segredo obrigatoria", 500);
        OffsetDateTime inicioNormalizado = normalizarData(validadeInicio, "Inicio da validade obrigatorio");
        OffsetDateTime fimNormalizado = normalizarData(validadeFim, "Fim da validade obrigatorio");
        validarValidade(inicioNormalizado, fimNormalizado);

        var atual = buscarRegistroAtivo(tenantId, filialId);
        if (atual.isPresent() && atual.get().equivale(
                tipoNormalizado, titularNormalizado, documentoNormalizado, serieNormalizada,
                thumbprintNormalizado, inicioNormalizado, fimNormalizado,
                cofreNormalizado, referenciaNormalizada)) {
            return atual.get().resultado(true);
        }

        jdbc.update("""
                UPDATE fiscal_certificados_digitais
                SET ativo = FALSE, atualizado_em = CURRENT_TIMESTAMP
                WHERE tenant_id = ? AND filial_id = ? AND ativo = TRUE
                """, tenantId, filialId);

        UUID certificadoId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO fiscal_certificados_digitais (
                    id, tenant_id, filial_id, tipo, titular, documento_titular,
                    numero_serie, thumbprint_sha256, validade_inicio, validade_fim,
                    cofre_segredos, referencia_segredo, ativo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)
                """, certificadoId, tenantId, filialId, tipoNormalizado, titularNormalizado,
                documentoNormalizado, serieNormalizada, thumbprintNormalizado,
                inicioNormalizado, fimNormalizado, cofreNormalizado, referenciaNormalizada);

        auditoria.registrar(tenantId, usuarioId, empresaId, filialId,
                atual.isPresent() ? "SUBSTITUIR_CERTIFICADO" : "CADASTRAR_CERTIFICADO",
                "FISCAL_CERTIFICADO_DIGITAL", certificadoId,
                "tipo=" + tipoNormalizado + ";validadeFim=" + fimNormalizado);
        return new Resultado(certificadoId, filialId, tipoNormalizado, titularNormalizado,
                documentoMascarado(documentoNormalizado), serieNormalizada,
                thumbprintMascarado(thumbprintNormalizado), inicioNormalizado,
                fimNormalizado, cofreNormalizado, true, false);
    }

    private java.util.Optional<RegistroInterno> buscarRegistroAtivo(UUID tenantId, UUID filialId) {
        return jdbc.query("""
                SELECT id, filial_id, tipo, titular, documento_titular, numero_serie,
                       thumbprint_sha256, validade_inicio, validade_fim,
                       cofre_segredos, referencia_segredo, ativo
                FROM fiscal_certificados_digitais
                WHERE tenant_id = ? AND filial_id = ? AND ativo = TRUE
                """, (rs, n) -> new RegistroInterno(
                        rs.getObject("id", UUID.class),
                        rs.getObject("filial_id", UUID.class),
                        rs.getString("tipo"),
                        rs.getString("titular"),
                        rs.getString("documento_titular"),
                        rs.getString("numero_serie"),
                        rs.getString("thumbprint_sha256"),
                        rs.getObject("validade_inicio", OffsetDateTime.class),
                        rs.getObject("validade_fim", OffsetDateTime.class),
                        rs.getString("cofre_segredos"),
                        rs.getString("referencia_segredo"),
                        rs.getBoolean("ativo")),
                tenantId, filialId).stream().findFirst();
    }

    String normalizarTipo(String tipo) {
        String normalizado = obrigatorio(tipo, "Tipo de certificado obrigatorio", 5)
                .toUpperCase(Locale.ROOT);
        if (!normalizado.equals("A1") && !normalizado.equals("A3")) {
            throw new IllegalArgumentException("Tipo de certificado invalido");
        }
        return normalizado;
    }

    String somenteDigitos(String documento) {
        String normalizado = documento == null ? "" : documento.replaceAll("\\D", "");
        if (normalizado.length() != 11 && normalizado.length() != 14) {
            throw new IllegalArgumentException("CPF ou CNPJ do titular invalido");
        }
        return normalizado;
    }

    String normalizarThumbprint(String thumbprint) {
        String normalizado = obrigatorio(thumbprint, "Thumbprint obrigatorio", 64)
                .toLowerCase(Locale.ROOT);
        if (!normalizado.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Thumbprint SHA-256 invalido");
        }
        return normalizado;
    }

    private String obrigatorio(String valor, String mensagem, int limite) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(mensagem);
        String normalizado = valor.trim();
        if (normalizado.length() > limite) throw new IllegalArgumentException(mensagem);
        return normalizado;
    }

    private OffsetDateTime normalizarData(OffsetDateTime valor, String mensagem) {
        if (valor == null) throw new IllegalArgumentException(mensagem);
        return valor.withOffsetSameInstant(ZoneOffset.UTC);
    }

    private void validarValidade(OffsetDateTime inicio, OffsetDateTime fim) {
        OffsetDateTime agora = OffsetDateTime.now(ZoneOffset.UTC);
        if (!fim.isAfter(inicio)) throw new IllegalArgumentException("Periodo de validade invalido");
        if (inicio.isAfter(agora.plusMinutes(5))) {
            throw new IllegalArgumentException("Certificado ainda nao esta valido");
        }
        if (!fim.isAfter(agora)) throw new IllegalArgumentException("Certificado expirado");
    }

    private String documentoMascarado(String documento) {
        return "*".repeat(documento.length() - 4) + documento.substring(documento.length() - 4);
    }

    private String thumbprintMascarado(String thumbprint) {
        return thumbprint.substring(0, 8) + "..." + thumbprint.substring(thumbprint.length() - 8);
    }

    private record RegistroInterno(
            UUID id, UUID filialId, String tipo, String titular, String documentoTitular,
            String numeroSerie, String thumbprint, OffsetDateTime validadeInicio,
            OffsetDateTime validadeFim, String cofreSegredos, String referenciaSegredo,
            boolean ativo) {
        boolean equivale(String tipo, String titular, String documentoTitular,
                         String numeroSerie, String thumbprint, OffsetDateTime validadeInicio,
                         OffsetDateTime validadeFim, String cofreSegredos,
                         String referenciaSegredo) {
            return this.tipo.equals(tipo)
                    && this.titular.equals(titular)
                    && this.documentoTitular.equals(documentoTitular)
                    && this.numeroSerie.equals(numeroSerie)
                    && this.thumbprint.equals(thumbprint)
                    && this.validadeInicio.isEqual(validadeInicio)
                    && this.validadeFim.isEqual(validadeFim)
                    && this.cofreSegredos.equals(cofreSegredos)
                    && this.referenciaSegredo.equals(referenciaSegredo);
        }

        Resultado resultado(boolean repetida) {
            return new Resultado(id, filialId, tipo, titular,
                    "*".repeat(documentoTitular.length() - 4)
                            + documentoTitular.substring(documentoTitular.length() - 4),
                    numeroSerie, thumbprint.substring(0, 8) + "..."
                            + thumbprint.substring(thumbprint.length() - 8),
                    validadeInicio, validadeFim, cofreSegredos, ativo, repetida);
        }
    }

    public record Resultado(
            UUID id, UUID filialId, String tipo, String titular,
            String documentoTitularMascarado, String numeroSerie,
            String thumbprintMascarado, OffsetDateTime validadeInicio,
            OffsetDateTime validadeFim, String cofreSegredos,
            boolean ativo, boolean repetida) {}
}
