package com.traxup.tplug.erp.contabilidade;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpedArquivoS3AdapterTest {
    @Test
    void repeticaoComMesmoHashNaoSobrescreveObjeto() {
        S3Client s3 = mock(S3Client.class);
        when(s3.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder()
                        .metadata(Map.of("sha256", "hash-igual"))
                        .build());
        var adapter = new SpedArquivoS3Adapter(s3, "sped");

        assertDoesNotThrow(() -> adapter.armazenar(
                "tenants/t/sped/arquivo.txt",
                new byte[]{1}, "hash-igual"));

        verify(s3, never()).putObject(
                any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void objetoExistenteComHashDiferenteEBloqueado() {
        S3Client s3 = mock(S3Client.class);
        when(s3.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder()
                        .metadata(Map.of("sha256", "hash-anterior"))
                        .build());
        var adapter = new SpedArquivoS3Adapter(s3, "sped");

        assertThrows(IllegalStateException.class,
                () -> adapter.armazenar(
                        "tenants/t/sped/arquivo.txt",
                        new byte[]{2}, "hash-novo"));

        verify(s3, never()).putObject(
                any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void objetoAusentePodeSerArmazenado() {
        S3Client s3 = mock(S3Client.class);
        when(s3.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(404).build());
        var adapter = new SpedArquivoS3Adapter(s3, "sped");

        adapter.armazenar(
                "tenants/t/sped/arquivo.txt",
                new byte[]{1}, "hash-novo");

        verify(s3).putObject(
                any(PutObjectRequest.class), any(RequestBody.class));
    }
}
