package com.traxup.tplug.erp.contabilidade;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(prefix = "contabilidade.sped.worker", name = "enabled",
        havingValue = "true")
public class SpedArquivoS3Configuration {
    @Bean
    SpedArquivoStoragePort spedArquivoStoragePort(
            S3Client fiscalS3Client,
            @Value("${contabilidade.sped.storage.bucket:${fiscal.storage.bucket}}")
            String bucket) {
        if (bucket == null || bucket.isBlank())
            throw new IllegalStateException(
                    "Bucket SPED obrigatorio quando o worker estiver habilitado");
        return new SpedArquivoS3Adapter(fiscalS3Client, bucket);
    }
}
