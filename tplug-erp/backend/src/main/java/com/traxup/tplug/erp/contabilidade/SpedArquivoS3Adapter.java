package com.traxup.tplug.erp.contabilidade;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Map;

public class SpedArquivoS3Adapter implements SpedArquivoStoragePort {
    private final S3Client s3;
    private final String bucket;

    public SpedArquivoS3Adapter(S3Client s3, String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @Override
    public void armazenar(String chave, byte[] conteudo, String hashSha256) {
        if (objetoJaArmazenado(chave, hashSha256)) return;

        var requisicao = PutObjectRequest.builder()
                .bucket(bucket)
                .key(chave)
                .contentType("text/plain")
                .metadata(Map.of("sha256", hashSha256))
                .build();
        s3.putObject(requisicao, RequestBody.fromBytes(conteudo));
    }

    private boolean objetoJaArmazenado(String chave, String hashSha256) {
        try {
            var resposta = s3.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(chave)
                    .build());
            String hashExistente = resposta.metadata().get("sha256");
            if (!hashSha256.equals(hashExistente))
                throw new IllegalStateException(
                        "Objeto SPED existente possui hash divergente");
            return true;
        } catch (S3Exception erro) {
            if (erro.statusCode() == 404) return false;
            throw erro;
        }
    }

    @Override
    public byte[] baixar(String chave) {
        var requisicao = GetObjectRequest.builder()
                .bucket(bucket)
                .key(chave)
                .build();
        return s3.getObjectAsBytes(requisicao).asByteArray();
    }
}
