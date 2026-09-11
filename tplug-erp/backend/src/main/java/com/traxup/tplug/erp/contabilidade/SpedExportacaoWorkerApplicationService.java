package com.traxup.tplug.erp.contabilidade;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class SpedExportacaoWorkerApplicationService {
    private static final String ERRO_PROCESSAMENTO =
            "GERACAO_OU_ARQUIVAMENTO";

    private final JdbcTemplate jdbc;
    private final ObjectProvider<SpedGeradorPort> geradorProvider;
    private final ObjectProvider<SpedArquivoStoragePort> storageProvider;
    private final SpedHomologacaoApplicationService homologacao;
    private final int maxTentativas;

    public SpedExportacaoWorkerApplicationService(
            JdbcTemplate jdbc,
            ObjectProvider<SpedGeradorPort> geradorProvider,
            ObjectProvider<SpedArquivoStoragePort> storageProvider,
            SpedHomologacaoApplicationService homologacao,
            @Value("${contabilidade.sped.worker.max-tentativas:5}")
            int maxTentativas) {
        this.jdbc = jdbc;
        this.geradorProvider = geradorProvider;
        this.storageProvider = storageProvider;
        this.homologacao = homologacao;
        this.maxTentativas = normalizarMaxTentativas(maxTentativas);
    }

    @Transactional
    public Resultado processar(UUID tenantId, Integer limiteSolicitado) {
        int limite = normalizarLimite(limiteSolicitado);
        SpedGeradorPort gerador = geradorProvider.getIfAvailable();
        SpedArquivoStoragePort storage = storageProvider.getIfAvailable();
        if (gerador == null)
            throw new IllegalStateException(
                    "Gerador SPED homologado nao configurado neste ambiente");
        if (storage == null)
            throw new IllegalStateException(
                    "Repositorio SPED nao configurado neste ambiente");

        List<Item> itens = jdbc.query("""
                SELECT id, tipo, competencia
                FROM contabilidade_sped_exportacoes
                WHERE tenant_id = ?
                  AND tentativas_processamento < ?
                  AND (
                      status IN ('PENDENTE', 'FALHOU')
                      OR (
                          status = 'PROCESSANDO'
                          AND atualizado_em
                              < CURRENT_TIMESTAMP - INTERVAL '15 minutes'
                      )
                  )
                ORDER BY criado_em, id
                FOR UPDATE SKIP LOCKED
                LIMIT ?
                """, (rs, n) -> new Item(
                        rs.getObject("id", UUID.class),
                        rs.getString("tipo"),
                        YearMonth.from(
                                rs.getDate("competencia").toLocalDate())),
                tenantId, maxTentativas, limite);

        int concluidos = 0;
        int falhas = 0;
        for (Item item : itens) {
            jdbc.update("""
                    UPDATE contabilidade_sped_exportacoes
                    SET status = 'PROCESSANDO',
                        tentativas_processamento =
                            tentativas_processamento + 1,
                        chave_objeto = NULL,
                        hash_sha256 = NULL,
                        versao_layout = NULL,
                        erro_codigo = NULL,
                        concluido_em = NULL,
                        atualizado_em = CURRENT_TIMESTAMP
                    WHERE tenant_id = ? AND id = ?
                    """, tenantId, item.id());
            try {
                SpedGeradorPort.Artefato artefato = gerador.gerar(
                        tenantId, item.tipo(), item.competencia());
                validarArtefato(artefato);
                homologacao.validar(artefato);
                String hash = sha256(artefato.conteudo());
                String chave = chaveObjeto(tenantId, item);
                storage.armazenar(chave, artefato.conteudo(), hash);
                jdbc.update("""
                        UPDATE contabilidade_sped_exportacoes
                        SET status = 'CONCLUIDO',
                            chave_objeto = ?,
                            hash_sha256 = ?,
                            versao_layout = ?,
                            erro_codigo = NULL,
                            concluido_em = CURRENT_TIMESTAMP,
                            atualizado_em = CURRENT_TIMESTAMP
                        WHERE tenant_id = ? AND id = ?
                        """, chave, hash, artefato.versaoLayout().trim(),
                        tenantId, item.id());
                concluidos++;
            } catch (RuntimeException erro) {
                jdbc.update("""
                        UPDATE contabilidade_sped_exportacoes
                        SET status = 'FALHOU',
                            chave_objeto = NULL,
                            hash_sha256 = NULL,
                            versao_layout = NULL,
                            erro_codigo = ?,
                            concluido_em = NULL,
                            atualizado_em = CURRENT_TIMESTAMP
                        WHERE tenant_id = ? AND id = ?
                        """, ERRO_PROCESSAMENTO, tenantId, item.id());
                falhas++;
            }
        }
        return new Resultado(itens.size(), concluidos, falhas);
    }

    static int normalizarLimite(Integer limite) {
        if (limite == null) return 5;
        if (limite < 1 || limite > 20)
            throw new IllegalArgumentException(
                    "Limite do worker deve estar entre 1 e 20");
        return limite;
    }

    static int normalizarMaxTentativas(int maxTentativas) {
        if (maxTentativas < 1 || maxTentativas > 20)
            throw new IllegalArgumentException(
                    "Maximo de tentativas deve estar entre 1 e 20");
        return maxTentativas;
    }

    static void validarArtefato(SpedGeradorPort.Artefato artefato) {
        if (artefato == null || artefato.conteudo() == null
                || artefato.conteudo().length == 0)
            throw new IllegalArgumentException(
                    "Gerador SPED retornou conteudo vazio");
        if (artefato.versaoLayout() == null
                || artefato.versaoLayout().isBlank()
                || artefato.versaoLayout().trim().length() > 40)
            throw new IllegalArgumentException(
                    "Gerador SPED retornou versao de layout invalida");
    }

    static String sha256(byte[] conteudo) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(conteudo));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    static String chaveObjeto(UUID tenantId, Item item) {
        return "tenants/" + tenantId + "/sped/" + item.tipo() + "/"
                + item.competencia() + "/" + item.id() + ".txt";
    }

    record Item(UUID id, String tipo, YearMonth competencia) {}

    public record Resultado(int processados, int concluidos, int falhas) {}
}
