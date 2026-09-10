package com.traxup.tplug.erp.fiscal;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Map;

public class FiscalArquivoS3Adapter implements FiscalArquivoStoragePort {
    private final S3Client s3;
    private final String bucket;

    public FiscalArquivoS3Adapter(S3Client s3, String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @Override
    public void armazenar(String chave, byte[] conteudo, String hashSha256) {
        var requisicao = PutObjectRequest.builder()
                .bucket(bucket)
                .key(chave)
                .contentType("application/xml")
                .metadata(Map.of("sha256", hashSha256))
                .build();
        s3.putObject(requisicao, RequestBody.fromBytes(conteudo));
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
