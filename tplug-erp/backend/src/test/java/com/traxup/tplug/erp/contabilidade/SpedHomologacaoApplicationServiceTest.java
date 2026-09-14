package com.traxup.tplug.erp.contabilidade;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpedHomologacaoApplicationServiceTest {
    @Test
    void normalizaListaDeVersoesHomologadas() {
        assertEquals(Set.of("019", "020"),
                SpedHomologacaoApplicationService
                        .parsearVersoes(" 019,020,019 "));
        assertTrue(SpedHomologacaoApplicationService
                .parsearVersoes(" ").isEmpty());
    }

    @Test
    void bloqueiaConfiguracaoAusente() {
        var semProvedor = new SpedHomologacaoApplicationService(
                " ", "019");
        assertFalse(semProvedor.configurada());
        assertThrows(IllegalStateException.class,
                () -> semProvedor.validar(
                        artefato("PROVEDOR_HOMOLOGADO", "019")));
    }

    @Test
    void bloqueiaProvedorNaoHomologado() {
        var homologacao = new SpedHomologacaoApplicationService(
                "PROVEDOR_HOMOLOGADO", "019");

        assertThrows(IllegalStateException.class,
                () -> homologacao.validar(
                        artefato("OUTRO_PROVEDOR", "019")));
    }

    @Test
    void bloqueiaVersaoNaoHomologada() {
        var homologacao = new SpedHomologacaoApplicationService(
                "PROVEDOR_HOMOLOGADO", "019");
        assertTrue(homologacao.configurada());

        assertThrows(IllegalStateException.class,
                () -> homologacao.validar(
                        artefato("PROVEDOR_HOMOLOGADO", "020")));
    }

    @Test
    void aceitaSomenteProvedorEVersaoExplicitamenteHomologados() {
        var homologacao = new SpedHomologacaoApplicationService(
                "PROVEDOR_HOMOLOGADO", "019,020");

        assertTrue(homologacao.provedorHomologado(
                " PROVEDOR_HOMOLOGADO "));
        assertFalse(homologacao.provedorHomologado("OUTRO_PROVEDOR"));
        homologacao.validar(
                artefato(" PROVEDOR_HOMOLOGADO ", " 020 "));
    }

    @Test
    void bloqueiaGeradorQueNaoDeclaraProvedorHomologado() {
        var homologacao = new SpedHomologacaoApplicationService(
                "PROVEDOR_HOMOLOGADO", "019");
        SpedGeradorPort semIdentidade =
                (tenantId, tipo, competencia) ->
                        artefato("PROVEDOR_HOMOLOGADO", "019");

        assertThrows(IllegalStateException.class,
                () -> homologacao.validarGerador(semIdentidade));
    }

    private static SpedGeradorPort.Artefato artefato(
            String provedorId,
            String versaoLayout) {
        return new SpedGeradorPort.Artefato(
                new byte[]{1}, provedorId, versaoLayout);
    }
}
