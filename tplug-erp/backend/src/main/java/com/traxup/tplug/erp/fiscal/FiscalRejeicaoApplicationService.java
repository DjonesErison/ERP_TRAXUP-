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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class FiscalRejeicaoApplicationService {
    private static final Set<String> ORIGENS = Set.of(
            "VALIDACAO_LOCAL", "ASSINATURA", "TRANSMISSAO", "SEFAZ");
    private static final Set<String> CATEGORIAS = Set.of(
            "CADASTRO", "TRIBUTACAO", "NUMERACAO", "CERTIFICADO",
            "ASSINATURA", "COMUNICACAO", "AUTORIZADOR", "NAO_CLASSIFICADA");
    private static final Pattern CAMPO = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.]{0,79}$");

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuditoriaApplicationService auditoria;

    public FiscalRejeicaoApplicationService(JdbcTemplate jdbc,
                                            AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado registrar(UUID tenantId, UUID usuarioId, UUID documentoId,
                               UUID transmissaoId, String origem, String codigo,
                               String mensagem, String categoria, boolean corrigivel,
                               List<String> camposCorrecao) {
        String origemNormalizada = normalizar(origem, ORIGENS, "Origem de rejeicao invalida");
        String categoriaNormalizada = normalizar(
                categoria, CATEGORIAS, "Categoria de rejeicao invalida");
        String codigoNormalizado = texto(codigo, 20, "Codigo de rejeicao obrigatorio");
        String mensagemNormalizada = texto(mensagem, 500, "Mensagem de rejeicao obrigatoria");
        List<String> campos = validarCampos(corrigivel, camposCorrecao);

        Documento documento = jdbc.query("""
                SELECT id, solicitacao_id, filial_id, ambiente
                FROM fiscal_documentos
                WHERE tenant_id = ? AND id = ?
                """, (rs, n) -> new Documento(
                        rs.getObject("id", UUID.class),
                        rs.getObject("solicitacao_id", UUID.class),
                        rs.getObject("filial_id", UUID.class),
                        rs.getString("ambiente")), tenantId, documentoId)
                .stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Documento fiscal nao encontrado para o tenant"));

        if (transmissaoId != null) {
            Integer vinculos = jdbc.queryForObject("""
                    SELECT count(*) FROM fiscal_transmissoes
                    WHERE tenant_id = ? AND id = ? AND documento_id = ?
                    """, Integer.class, tenantId, transmissaoId, documentoId);
            if (vinculos == null || vinculos != 1) {
                throw new IllegalArgumentException(
                        "Transmissao nao pertence ao documento e tenant");
            }
        }

        UUID rejeicaoId = UUID.nameUUIDFromBytes((tenantId + ":" + documentoId + ":"
                + (transmissaoId == null ? "sem-transmissao" : transmissaoId) + ":"
                + origemNormalizada + ":" + codigoNormalizado).getBytes(StandardCharsets.UTF_8));
        String camposJson = json(campos);
        int inseridos = jdbc.update("""
                INSERT INTO fiscal_rejeicoes
                    (id, tenant_id, solicitacao_id, documento_id, transmissao_id,
                     ambiente, origem, codigo, mensagem, categoria, corrigivel,
                     status, campos_correcao)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ABERTA', CAST(? AS jsonb))
                ON CONFLICT (id) DO NOTHING
                """, rejeicaoId, tenantId, documento.solicitacaoId(), documentoId,
                transmissaoId, documento.ambiente(), origemNormalizada, codigoNormalizado,
                mensagemNormalizada, categoriaNormalizada, corrigivel, camposJson);

        Resultado resultado = buscar(tenantId, rejeicaoId);
        if (inseridos == 1) {
            auditoria.registrar(tenantId, usuarioId, null, documento.filialId(),
                    "REGISTRAR_REJEICAO_FISCAL", "FISCAL_REJEICAO", rejeicaoId,
                    "documentoId=" + documentoId + ";origem=" + origemNormalizada
                            + ";codigo=" + codigoNormalizado + ";categoria="
                            + categoriaNormalizada + ";corrigivel=" + corrigivel);
        }
        return new Resultado(resultado.rejeicao(), inseridos == 0);
    }

    @Transactional(readOnly = true)
    public List<Rejeicao> listar(UUID tenantId, UUID documentoId) {
        return jdbc.query("""
                SELECT id, solicitacao_id, documento_id, transmissao_id, ambiente,
                       origem, codigo, mensagem, categoria, corrigivel, status,
                       campos_correcao::text AS campos_correcao, detectada_em, resolvida_em
                FROM fiscal_rejeicoes
                WHERE tenant_id = ? AND documento_id = ?
                ORDER BY detectada_em DESC, id
                """, (rs, n) -> new Rejeicao(
                        rs.getObject("id", UUID.class),
                        rs.getObject("solicitacao_id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("transmissao_id", UUID.class),
                        rs.getString("ambiente"), rs.getString("origem"),
                        rs.getString("codigo"), rs.getString("mensagem"),
                        rs.getString("categoria"), rs.getBoolean("corrigivel"),
                        rs.getString("status"), lerCampos(rs.getString("campos_correcao")),
                        instante(rs.getTimestamp("detectada_em")),
                        instante(rs.getTimestamp("resolvida_em"))),
                tenantId, documentoId);
    }

    @Transactional
    public Resultado iniciarCorrecao(UUID tenantId, UUID usuarioId, UUID rejeicaoId) {
        Resultado atual = buscar(tenantId, rejeicaoId);
        if (!atual.rejeicao().corrigivel()) {
            throw new IllegalArgumentException("Rejeicao nao permite correcao");
        }
        if ("EM_CORRECAO".equals(atual.rejeicao().status())) {
            return new Resultado(atual.rejeicao(), true);
        }
        if (!"ABERTA".equals(atual.rejeicao().status())) {
            throw new IllegalArgumentException("Rejeicao ja foi encerrada");
        }

        int alterados = jdbc.update("""
                UPDATE fiscal_rejeicoes SET status = 'EM_CORRECAO'
                WHERE tenant_id = ? AND id = ? AND status = 'ABERTA' AND corrigivel = TRUE
                """, tenantId, rejeicaoId);
        Resultado resultado = buscar(tenantId, rejeicaoId);
        if (alterados == 1) {
            UUID filialId = jdbc.queryForObject("""
                    SELECT d.filial_id FROM fiscal_documentos d
                    JOIN fiscal_rejeicoes r
                      ON r.tenant_id = d.tenant_id AND r.documento_id = d.id
                    WHERE r.tenant_id = ? AND r.id = ?
                    """, UUID.class, tenantId, rejeicaoId);
            auditoria.registrar(tenantId, usuarioId, null, filialId,
                    "INICIAR_CORRECAO_REJEICAO_FISCAL", "FISCAL_REJEICAO", rejeicaoId,
                    "documentoId=" + resultado.rejeicao().documentoId()
                            + ";codigo=" + resultado.rejeicao().codigo());
        }
        return new Resultado(resultado.rejeicao(), alterados == 0);
    }

    private Resultado buscar(UUID tenantId, UUID rejeicaoId) {
        return jdbc.query("""
                SELECT id, solicitacao_id, documento_id, transmissao_id, ambiente,
                       origem, codigo, mensagem, categoria, corrigivel, status,
                       campos_correcao::text AS campos_correcao, detectada_em, resolvida_em
                FROM fiscal_rejeicoes WHERE tenant_id = ? AND id = ?
                """, (rs, n) -> new Resultado(new Rejeicao(
                        rs.getObject("id", UUID.class),
                        rs.getObject("solicitacao_id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("transmissao_id", UUID.class),
                        rs.getString("ambiente"), rs.getString("origem"),
                        rs.getString("codigo"), rs.getString("mensagem"),
                        rs.getString("categoria"), rs.getBoolean("corrigivel"),
                        rs.getString("status"), lerCampos(rs.getString("campos_correcao")),
                        instante(rs.getTimestamp("detectada_em")),
                        instante(rs.getTimestamp("resolvida_em"))), false),
                tenantId, rejeicaoId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Rejeicao fiscal nao encontrada para o tenant"));
    }

    static List<String> validarCampos(boolean corrigivel, List<String> valores) {
        if (!corrigivel) return List.of();
        if (valores == null || valores.isEmpty()) {
            throw new IllegalArgumentException(
                    "Rejeicao corrigivel deve informar ao menos um campo");
        }
        LinkedHashSet<String> unicos = new LinkedHashSet<>();
        for (String valor : valores) {
            if (valor == null || !CAMPO.matcher(valor.trim()).matches()) {
                throw new IllegalArgumentException("Nome de campo de correcao invalido");
            }
            unicos.add(valor.trim());
        }
        return List.copyOf(unicos);
    }

    private String normalizar(String valor, Set<String> permitidos, String mensagem) {
        if (valor == null) throw new IllegalArgumentException(mensagem);
        String normalizado = valor.trim().toUpperCase(Locale.ROOT);
        if (!permitidos.contains(normalizado)) throw new IllegalArgumentException(mensagem);
        return normalizado;
    }

    private String texto(String valor, int limite, String mensagem) {
        if (valor == null || valor.isBlank() || valor.trim().length() > limite) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim();
    }

    private String json(List<String> campos) {
        try {
            return objectMapper.writeValueAsString(campos);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar campos de correcao", e);
        }
    }

    private List<String> lerCampos(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao ler campos de correcao", e);
        }
    }

    private static Instant instante(Timestamp valor) {
        return valor == null ? null : valor.toInstant();
    }

    record Documento(UUID id, UUID solicitacaoId, UUID filialId, String ambiente) {}

    public record Rejeicao(UUID id, UUID solicitacaoId, UUID documentoId,
                           UUID transmissaoId, String ambiente, String origem,
                           String codigo, String mensagem, String categoria,
                           boolean corrigivel, String status, List<String> camposCorrecao,
                           Instant detectadaEm, Instant resolvidaEm) {}
    public record Resultado(Rejeicao rejeicao, boolean repetida) {}
}
