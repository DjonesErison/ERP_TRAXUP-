package com.traxup.tplug.erp.contabilidade;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SpedProntidaoApplicationService {
    private final ObjectProvider<SpedGeradorPort> geradorProvider;
    private final ObjectProvider<SpedArquivoStoragePort> storageProvider;
    private final SpedHomologacaoApplicationService homologacao;
    private final boolean workerHabilitado;

    public SpedProntidaoApplicationService(
            ObjectProvider<SpedGeradorPort> geradorProvider,
            ObjectProvider<SpedArquivoStoragePort> storageProvider,
            SpedHomologacaoApplicationService homologacao,
            @Value("${contabilidade.sped.worker.enabled:false}")
            boolean workerHabilitado) {
        this.geradorProvider = geradorProvider;
        this.storageProvider = storageProvider;
        this.homologacao = homologacao;
        this.workerHabilitado = workerHabilitado;
    }

    public Prontidao consultar() {
        return calcular(
                workerHabilitado,
                geradorProvider.getIfAvailable() != null,
                storageProvider.getIfAvailable() != null,
                homologacao.configurada());
    }

    static Prontidao calcular(
            boolean workerHabilitado,
            boolean geradorConfigurado,
            boolean repositorioConfigurado,
            boolean homologacaoConfigurada) {
        String pendencia;
        if (!workerHabilitado)
            pendencia = "WORKER_DESABILITADO";
        else if (!geradorConfigurado)
            pendencia = "GERADOR_HOMOLOGADO_NAO_CONFIGURADO";
        else if (!repositorioConfigurado)
            pendencia = "REPOSITORIO_NAO_CONFIGURADO";
        else if (!homologacaoConfigurada)
            pendencia = "HOMOLOGACAO_NAO_CONFIGURADA";
        else
            pendencia = null;

        return new Prontidao(
                workerHabilitado,
                geradorConfigurado,
                repositorioConfigurado,
                homologacaoConfigurada,
                pendencia == null,
                pendencia);
    }

    public record Prontidao(
            boolean workerHabilitado,
            boolean geradorHomologadoConfigurado,
            boolean repositorioConfigurado,
            boolean homologacaoConfigurada,
            boolean prontoParaProcessar,
            String pendenciaCodigo) {}
}
