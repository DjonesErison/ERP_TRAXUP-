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
    void bloqueiaProvedorOuVersaoNaoHomologados() {
        var semProvedor = new SpedHomologacaoApplicationService(
                " ", "019");
        assertFalse(semProvedor.configurada());
        assertThrows(IllegalStateException.class,
                () -> semProvedor.validar(
                        new SpedGeradorPort.Artefato(
                                new byte[]{1}, "019")));

        var homologacao = new SpedHomologacaoApplicationService(
                "PROVEDOR_HOMOLOGADO", "019");
        assertTrue(homologacao.configurada());
        assertThrows(IllegalStateException.class,
                () -> homologacao.validar(
                        new SpedGeradorPort.Artefato(
                                new byte[]{1}, "020")));
    }

    @Test
    void aceitaSomenteVersaoExplicitamenteHomologada() {
        var homologacao = new SpedHomologacaoApplicationService(
                "PROVEDOR_HOMOLOGADO", "019,020");

        homologacao.validar(new SpedGeradorPort.Artefato(
                new byte[]{1}, " 020 "));
    }
}
