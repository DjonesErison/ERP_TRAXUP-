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
        SpedGeradorPort gerador = geradorProvider.getIfAvailable();
        boolean geradorConfigurado = gerador != null;
        boolean provedorHomologado = geradorConfigurado
                && homologacao.provedorHomologado(gerador.provedorId());
        return calcular(
                workerHabilitado,
                geradorConfigurado,
                storageProvider.getIfAvailable() != null,
                homologacao.configurada(),
                provedorHomologado);
    }

    static Prontidao calcular(
            boolean workerHabilitado,
            boolean geradorHomologadoConfigurado,
            boolean repositorioConfigurado,
            boolean homologacaoConfigurada,
            boolean provedorHomologado) {
        String pendencia;
        if (!workerHabilitado)
            pendencia = "WORKER_DESABILITADO";
        else if (!geradorConfigurado)
            pendencia = "GERADOR_HOMOLOGADO_NAO_CONFIGURADO";
        else if (!repositorioConfigurado)
            pendencia = "REPOSITORIO_NAO_CONFIGURADO";
        else if (!homologacaoConfigurada)
            pendencia = "HOMOLOGACAO_NAO_CONFIGURADA";
        else if (!provedorHomologado)
            pendencia = "PROVEDOR_NAO_HOMOLOGADO";
        else
            pendencia = null;

        return new Prontidao(
                workerHabilitado,
                geradorConfigurado,
                repositorioConfigurado,
                homologacaoConfigurada,
                provedorHomologado,
                pendencia == null,
                pendencia);
    }

    public record Prontidao(
            boolean workerHabilitado,
            boolean geradorConfigurado,
            boolean repositorioConfigurado,
            boolean homologacaoConfigurada,
            boolean provedorHomologado,
            boolean prontoParaProcessar,
            String pendenciaCodigo) {}
}
