package com.traxup.tplug.erp.fiscal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@ConditionalOnProperty(prefix = "fiscal.storage", name = "enabled",
        havingValue = "true")
public class FiscalArquivoS3Configuration {
    @Bean
    S3Client fiscalS3Client(
            @Value("${fiscal.storage.region}") String region,
            @Value("${fiscal.storage.endpoint:}") String endpoint,
            @Value("${fiscal.storage.path-style:true}") boolean pathStyle) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyle).build());
        if (endpoint != null && !endpoint.isBlank())
            builder.endpointOverride(URI.create(endpoint));
        return builder.build();
    }

    @Bean
    FiscalArquivoStoragePort fiscalArquivoStoragePort(
            S3Client fiscalS3Client,
            @Value("${fiscal.storage.bucket}") String bucket) {
        if (bucket == null || bucket.isBlank())
            throw new IllegalStateException(
                    "FISCAL_STORAGE_BUCKET obrigatorio quando storage estiver habilitado");
        return new FiscalArquivoS3Adapter(fiscalS3Client, bucket);
    }
}
