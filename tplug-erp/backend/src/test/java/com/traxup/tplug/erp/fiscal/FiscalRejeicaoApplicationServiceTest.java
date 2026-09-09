package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FiscalRejeicaoApplicationServiceTest {
    @Test
    void normalizaRemoveDuplicidadeEPreservaOrdemDosCampos() {
        assertEquals(List.of("produto.ncm", "tributacao.cfop"),
                FiscalRejeicaoApplicationService.validarCampos(true,
                        List.of("produto.ncm", "tributacao.cfop", "produto.ncm")));
    }

    @Test
    void rejeicaoNaoCorrigivelNaoAceitaNemPersisteCampos() {
        assertEquals(List.of(),
                FiscalRejeicaoApplicationService.validarCampos(false,
                        List.of("produto.ncm")));
    }

    @Test
    void rejeitaCampoQuePoderiaInjetarEstruturaOuValor() {
        assertThrows(IllegalArgumentException.class,
                () -> FiscalRejeicaoApplicationService.validarCampos(
                        true, List.of("produto.ncm=value")));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalRejeicaoApplicationService.validarCampos(
                        true, List.of("campo\"malicioso")));
    }

    @Test
    void exigeCampoQuandoRejeicaoForCorrigivel() {
        assertThrows(IllegalArgumentException.class,
                () -> FiscalRejeicaoApplicationService.validarCampos(true, List.of()));
    }
}
