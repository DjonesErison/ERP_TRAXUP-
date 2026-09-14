package com.traxup.tplug.erp.contabilidade;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SpedHomologacaoApplicationService {
    private final String provedorId;
    private final Set<String> versoesPermitidas;

    public SpedHomologacaoApplicationService(
            @Value("${contabilidade.sped.homologacao.provedor-id:}")
            String provedorId,
            @Value("${contabilidade.sped.homologacao.versoes-layout:}")
            String versoesLayout) {
        this.provedorId = provedorId == null ? "" : provedorId.trim();
        this.versoesPermitidas = parsearVersoes(versoesLayout);
    }

    public boolean configurada() {
        return !provedorId.isBlank() && !versoesPermitidas.isEmpty();
    }

    public boolean provedorHomologado(String identificador) {
        return configurada()
                && identificador != null
                && provedorId.equals(identificador.trim());
    }

    public void validarGerador(SpedGeradorPort gerador) {
        if (!configurada())
            throw new IllegalStateException(
                    "Homologacao SPED nao configurada");
        if (gerador == null || !provedorHomologado(gerador.provedorId()))
            throw new IllegalStateException(
                    "Provedor SPED nao homologado");
    }

    public void validar(SpedGeradorPort.Artefato artefato) {
        if (!configurada())
            throw new IllegalStateException(
                    "Homologacao SPED nao configurada");
        if (!provedorHomologado(artefato.provedorId()))
            throw new IllegalStateException(
                    "Provedor SPED nao homologado");
        String versao = artefato.versaoLayout().trim();
        if (!versoesPermitidas.contains(versao))
            throw new IllegalStateException(
                    "Versao do layout SPED nao homologada");
    }

    static Set<String> parsearVersoes(String valor) {
        if (valor == null || valor.isBlank()) return Set.of();
        return Arrays.stream(valor.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }
}
