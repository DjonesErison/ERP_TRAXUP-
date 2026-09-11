package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.contabilidade.api.SpedExportacaoController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpedExportacaoApplicationServiceTest {
    @Test
    void aceitaSomenteTiposSpedSuportados() {
        assertEquals("EFD_ICMS_IPI",
                SpedExportacaoApplicationService
                        .normalizarTipo(" efd_icms_ipi "));
        assertEquals("EFD_CONTRIBUICOES",
                SpedExportacaoApplicationService
                        .normalizarTipo("efd_contribuicoes"));
        assertThrows(IllegalArgumentException.class,
                () -> SpedExportacaoApplicationService
                        .normalizarTipo("SPED_DESCONHECIDO"));
    }

    @Test
    void limitaVolumeDaListagem() {
        assertEquals(100,
                SpedExportacaoApplicationService.validarLimite(null));
        assertEquals(500,
                SpedExportacaoApplicationService.validarLimite(500));
        assertThrows(IllegalArgumentException.class,
                () -> SpedExportacaoApplicationService.validarLimite(0));
        assertThrows(IllegalArgumentException.class,
                () -> SpedExportacaoApplicationService.validarLimite(501));
    }

    @Test
    void protegeSolicitacaoEListagemComPermissaoDedicada()
            throws NoSuchMethodException {
        String regraEsperada =
                "hasAuthority('CONTABILIDADE_SPED_SOLICITAR')";

        PreAuthorize post = SpedExportacaoController.class
                .getDeclaredMethod("solicitar",
                        SpedExportacaoController
                                .SolicitarExportacaoRequest.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize get = SpedExportacaoController.class
                .getDeclaredMethod("listar", String.class, Integer.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals(regraEsperada, post.value());
        assertEquals(regraEsperada, get.value());
    }
}
