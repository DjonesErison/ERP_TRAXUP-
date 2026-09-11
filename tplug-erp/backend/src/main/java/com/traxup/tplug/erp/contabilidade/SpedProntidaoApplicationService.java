package com.traxup.tplug.erp.contabilidade;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SpedProntidaoApplicationService {
    private final ObjectProvider<SpedGeradorPort> geradorProvider;
    private final ObjectProvider<SpedArquivoStoragePort> storageProvider;
    private final boolean workerHabilitado;

    public SpedProntidaoApplicationService(
            ObjectProvider<SpedGeradorPort> geradorProvider,
            ObjectProvider<SpedArquivoStoragePort> storageProvider,
            @Value("${contabilidade.sped.worker.enabled:false}")
            boolean workerHabilitado) {
        this.geradorProvider = geradorProvider;
        this.storageProvider = storageProvider;
        this.workerHabilitado = workerHabilitado;
    }

    public Prontidao consultar() {
        return calcular(
                workerHabilitado,
                geradorProvider.getIfAvailable() != null,
                storageProvider.getIfAvailable() != null);
    }

    static Prontidao calcular(
            boolean workerHabilitado,
            boolean geradorConfigurado,
            boolean repositorioConfigurado) {
        String pendencia;
        if (!workerHabilitado)
            pendencia = "WORKER_DESABILITADO";
        else if (!geradorConfigurado)
            pendencia = "GERADOR_HOMOLOGADO_NAO_CONFIGURADO";
        else if (!repositorioConfigurado)
            pendencia = "REPOSITORIO_NAO_CONFIGURADO";
        else
            pendencia = null;

        return new Prontidao(
                workerHabilitado,
                geradorConfigurado,
                repositorioConfigurado,
                pendencia == null,
                pendencia);
    }

    public record Prontidao(
            boolean workerHabilitado,
            boolean geradorHomologadoConfigurado,
            boolean repositorioConfigurado,
            boolean prontoParaProcessar,
            String pendenciaCodigo) {}
}
