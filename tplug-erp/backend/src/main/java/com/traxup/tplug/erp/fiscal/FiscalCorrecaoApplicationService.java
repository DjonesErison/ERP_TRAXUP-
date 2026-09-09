package com.traxup.tplug.erp.fiscal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class FiscalCorrecaoApplicationService {
    private static final Set<String> CAMPOS_SUPORTADOS = Set.of(
            "tributacao.cfop", "tributacao.cstIcms",
            "tributacao.csosn", "tributacao.ufDestino");
    private static final Pattern CFOP = Pattern.compile("^[0-9]{4}$");
    private static final Pattern CST = Pattern.compile("^[0-9]{2,3}$");
    private static final Pattern CSOSN = Pattern.compile("^[0-9]{3}$");
    private static final Pattern UF = Pattern.compile("^[A-Z]{2}$");

    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FiscalCorrecaoApplicationService(JdbcTemplate jdbc,
                                            AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    @Transactional
    public Correcao registrar(UUID tenantId, UUID usuarioId, UUID rejeicaoId,
                              Map<String, Object> valores, String motivo) {
        Rejeicao rejeicao = bloquearRejeicao(tenantId, rejeicaoId);
        if (!rejeicao.corrigivel() || !"EM_CORRECAO".equals(rejeicao.status())) {
            throw new IllegalArgumentException(
                    "Rejeicao deve ser corrigivel e estar EM_CORRECAO");
        }

        Map<String, String> normalizados = validarValores(valores);
        if (!rejeicao.camposPermitidos().containsAll(normalizados.keySet())) {
            throw new IllegalArgumentException(
                    "Correcao contem campo nao autorizado pela rejeicao");
        }
        String motivoNormalizado = validarMotivo(motivo);
        Integer versao = jdbc.queryForObject("""
                SELECT COALESCE(MAX(versao), 0) + 1
                FROM fiscal_rejeicao_correcoes
                WHERE tenant_id = ? AND rejeicao_id = ?
                """, Integer.class, tenantId, rejeicaoId);
        String camposJson = json(List.copyOf(normalizados.keySet()));
        String valoresJson = json(normalizados);
        String hash = sha256(rejeicaoId + ":" + versao + ":"
                + valoresJson + ":" + motivoNormalizado);
        UUID correcaoId = UUID.nameUUIDFromBytes(
                (rejeicaoId + ":correcao:" + versao).getBytes(StandardCharsets.UTF_8));

        jdbc.update("""
                INSERT INTO fiscal_rejeicao_correcoes
                    (id, tenant_id, rejeicao_id, documento_id, versao,
                     campos_corrigidos, valores_corrigidos, motivo, hash_sha256, criado_por)
                VALUES (?, ?, ?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb), ?, ?, ?)
                """, correcaoId, tenantId, rejeicaoId, rejeicao.documentoId(), versao,
                camposJson, valoresJson, motivoNormalizado, hash, usuarioId);
        auditoria.registrar(tenantId, usuarioId, null, rejeicao.filialId(),
                "REGISTRAR_CORRECAO_REJEICAO_FISCAL", "FISCAL_REJEICAO_CORRECAO",
                correcaoId, "rejeicaoId=" + rejeicaoId + ";documentoId="
                        + rejeicao.documentoId() + ";versao=" + versao
                        + ";campos=" + String.join(",", normalizados.keySet())
                        + ";hash=" + hash);
        return buscar(tenantId, correcaoId);
    }

    @Transactional(readOnly = true)
    public List<Correcao> listar(UUID tenantId, UUID rejeicaoId) {
        return jdbc.query("""
                SELECT c.id, c.rejeicao_id, c.documento_id, c.versao,
                       c.campos_corrigidos::text AS campos, c.valores_corrigidos::text AS valores,
                       c.motivo, c.hash_sha256, c.status, c.criado_por,
                       c.criado_em, c.aplicada_em
                FROM fiscal_rejeicao_correcoes c
                JOIN fiscal_rejeicoes r
                  ON r.tenant_id = c.tenant_id AND r.id = c.rejeicao_id
                WHERE c.tenant_id = ? AND c.rejeicao_id = ?
                ORDER BY c.versao DESC
                """, (rs, n) -> mapear(
                        rs.getObject("id", UUID.class),
                        rs.getObject("rejeicao_id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getInt("versao"), rs.getString("campos"),
                        rs.getString("valores"), rs.getString("motivo"),
                        rs.getString("hash_sha256"), rs.getString("status"),
                        rs.getObject("criado_por", UUID.class),
                        rs.getTimestamp("criado_em"), rs.getTimestamp("aplicada_em")),
                tenantId, rejeicaoId);
    }

    @Transactional
    public Correcao aplicar(UUID tenantId, UUID usuarioId, UUID correcaoId) {
        Correcao correcao = buscarParaAtualizar(tenantId, correcaoId);
        if ("APLICADA".equals(correcao.status())) return correcao;
        if (!"REGISTRADA".equals(correcao.status())) {
            throw new IllegalArgumentException("Somente correcao REGISTRADA pode ser aplicada");
        }
        Rejeicao rejeicao = bloquearRejeicao(tenantId, correcao.rejeicaoId());
        if (!"EM_CORRECAO".equals(rejeicao.status())) {
            throw new IllegalArgumentException("Rejeicao nao esta EM_CORRECAO");
        }

        Map<String, String> valores = validarValores(
                new TreeMap<>(correcao.valoresCorrigidos()));
        aplicarNoDocumento(tenantId, correcao.documentoId(), valores);
        jdbc.update("""
                UPDATE fiscal_rejeicao_correcoes
                SET status = 'DESCARTADA'
                WHERE tenant_id = ? AND rejeicao_id = ? AND status = 'REGISTRADA' AND id <> ?
                """, tenantId, correcao.rejeicaoId(), correcaoId);
        jdbc.update("""
                UPDATE fiscal_rejeicao_correcoes
                SET status = 'APLICADA', aplicada_em = CURRENT_TIMESTAMP
                WHERE tenant_id = ? AND id = ? AND status = 'REGISTRADA'
                """, tenantId, correcaoId);
        jdbc.update("""
                UPDATE fiscal_rejeicoes
                SET status = 'CORRIGIDA', resolvida_em = CURRENT_TIMESTAMP
                WHERE tenant_id = ? AND id = ? AND status = 'EM_CORRECAO'
                """, tenantId, correcao.rejeicaoId());

        auditoria.registrar(tenantId, usuarioId, null, rejeicao.filialId(),
                "APLICAR_CORRECAO_REJEICAO_FISCAL", "FISCAL_REJEICAO_CORRECAO",
                correcaoId, "rejeicaoId=" + correcao.rejeicaoId()
                        + ";documentoId=" + correcao.documentoId()
                        + ";versao=" + correcao.versao()
                        + ";campos=" + String.join(",", valores.keySet())
                        + ";hash=" + correcao.hashSha256());
        return buscar(tenantId, correcaoId);
    }

    private void aplicarNoDocumento(UUID tenantId, UUID documentoId,
                                    Map<String, String> valores) {
        String cfop = valores.get("tributacao.cfop");
        String cst = valores.get("tributacao.cstIcms");
        String csosn = valores.get("tributacao.csosn");
        String uf = valores.get("tributacao.ufDestino");
        int alterados = jdbc.update("""
                UPDATE fiscal_documentos SET
                    cfop = COALESCE(?, cfop),
                    cst_icms = CASE
                        WHEN ? IS NOT NULL THEN ?
                        WHEN ? IS NOT NULL THEN NULL
                        ELSE cst_icms END,
                    csosn = CASE
                        WHEN ? IS NOT NULL THEN ?
                        WHEN ? IS NOT NULL THEN NULL
                        ELSE csosn END,
                    uf_destino = COALESCE(?, uf_destino)
                WHERE tenant_id = ? AND id = ?
                """, cfop, cst, cst, csosn, csosn, csosn, cst,
                uf, tenantId, documentoId);
        if (alterados != 1) {
            throw new RecursoNaoEncontradoException(
                    "Documento fiscal nao encontrado para aplicar correcao");
        }
    }

    private Rejeicao bloquearRejeicao(UUID tenantId, UUID rejeicaoId) {
        return jdbc.query("""
                SELECT r.id, r.documento_id, r.corrigivel, r.status,
                       r.campos_correcao::text AS campos, d.filial_id
                FROM fiscal_rejeicoes r
                JOIN fiscal_documentos d
                  ON d.tenant_id = r.tenant_id AND d.id = r.documento_id
                WHERE r.tenant_id = ? AND r.id = ?
                FOR UPDATE OF r
                """, (rs, n) -> new Rejeicao(
                        rs.getObject("id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getBoolean("corrigivel"), rs.getString("status"),
                        Set.copyOf(lerLista(rs.getString("campos"))),
                        rs.getObject("filial_id", UUID.class)),
                tenantId, rejeicaoId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Rejeicao fiscal nao encontrada para o tenant"));
    }

    private Correcao buscarParaAtualizar(UUID tenantId, UUID correcaoId) {
        return consultarUma(tenantId, correcaoId, true);
    }

    private Correcao buscar(UUID tenantId, UUID correcaoId) {
        return consultarUma(tenantId, correcaoId, false);
    }

    private Correcao consultarUma(UUID tenantId, UUID correcaoId, boolean bloquear) {
        String sql = """
                SELECT id, rejeicao_id, documento_id, versao,
                       campos_corrigidos::text AS campos, valores_corrigidos::text AS valores,
                       motivo, hash_sha256, status, criado_por, criado_em, aplicada_em
                FROM fiscal_rejeicao_correcoes
                WHERE tenant_id = ? AND id = ?
                """ + (bloquear ? " FOR UPDATE" : "");
        return jdbc.query(sql, (rs, n) -> mapear(
                        rs.getObject("id", UUID.class),
                        rs.getObject("rejeicao_id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getInt("versao"), rs.getString("campos"),
                        rs.getString("valores"), rs.getString("motivo"),
                        rs.getString("hash_sha256"), rs.getString("status"),
                        rs.getObject("criado_por", UUID.class),
                        rs.getTimestamp("criado_em"), rs.getTimestamp("aplicada_em")),
                tenantId, correcaoId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Correcao fiscal nao encontrada para o tenant"));
    }

    static Map<String, String> validarValores(Map<String, ?> valores) {
        if (valores == null || valores.isEmpty()) {
            throw new IllegalArgumentException("Valores corrigidos sao obrigatorios");
        }
        TreeMap<String, String> resultado = new TreeMap<>();
        for (var item : valores.entrySet()) {
            String campo = item.getKey();
            if (!CAMPOS_SUPORTADOS.contains(campo) || !(item.getValue() instanceof String valor)) {
                throw new IllegalArgumentException("Campo ou tipo de correcao nao suportado");
            }
            String normalizado = valor.trim().toUpperCase();
            boolean valido = switch (campo) {
                case "tributacao.cfop" -> CFOP.matcher(normalizado).matches();
                case "tributacao.cstIcms" -> CST.matcher(normalizado).matches();
                case "tributacao.csosn" -> CSOSN.matcher(normalizado).matches();
                case "tributacao.ufDestino" -> UF.matcher(normalizado).matches();
                default -> false;
            };
            if (!valido) throw new IllegalArgumentException(
                    "Valor fiscal invalido para " + campo);
            resultado.put(campo, normalizado);
        }
        if (resultado.containsKey("tributacao.cstIcms")
                && resultado.containsKey("tributacao.csosn")) {
            throw new IllegalArgumentException("CST e CSOSN nao podem ser corrigidos juntos");
        }
        return Map.copyOf(resultado);
    }

    private String validarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank() || motivo.trim().length() > 500) {
            throw new IllegalArgumentException("Motivo da correcao e obrigatorio");
        }
        return motivo.trim();
    }

    private String json(Object valor) {
        try {
            return objectMapper.writeValueAsString(valor);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar correcao fiscal", e);
        }
    }

    private List<String> lerLista(String valor) {
        try {
            return objectMapper.readValue(valor, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao ler campos da rejeicao", e);
        }
    }

    private Map<String, String> lerMapa(String valor) {
        try {
            return objectMapper.readValue(valor, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao ler valores da correcao", e);
        }
    }

    private Correcao mapear(UUID id, UUID rejeicaoId, UUID documentoId, int versao,
                            String campos, String valores, String motivo, String hash,
                            String status, UUID criadoPor, Timestamp criadoEm,
                            Timestamp aplicadaEm) {
        return new Correcao(id, rejeicaoId, documentoId, versao, lerLista(campos),
                lerMapa(valores), motivo, hash, status, criadoPor,
                criadoEm.toInstant(), aplicadaEm == null ? null : aplicadaEm.toInstant());
    }

    private String sha256(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    record Rejeicao(UUID id, UUID documentoId, boolean corrigivel, String status,
                    Set<String> camposPermitidos, UUID filialId) {}

    public record Correcao(UUID id, UUID rejeicaoId, UUID documentoId, int versao,
                           List<String> camposCorrigidos,
                           Map<String, String> valoresCorrigidos, String motivo,
                           String hashSha256, String status, UUID criadoPor,
                           Instant criadoEm, Instant aplicadaEm) {}
}
