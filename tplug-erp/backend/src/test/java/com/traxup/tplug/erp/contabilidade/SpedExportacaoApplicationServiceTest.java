package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.contabilidade.api.SpedExportacaoController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void validaStatusEIntervaloDeCompetencias() {
        assertNull(SpedExportacaoApplicationService
                .normalizarStatusOpcional(" "));
        assertEquals("CONCLUIDO", SpedExportacaoApplicationService
                .normalizarStatusOpcional(" concluido "));
        assertEquals("CANCELADO", SpedExportacaoApplicationService
                .normalizarStatusOpcional("cancelado"));
        assertThrows(IllegalArgumentException.class,
                () -> SpedExportacaoApplicationService
                        .normalizarStatusOpcional("EXCLUIDO"));
        assertThrows(IllegalArgumentException.class,
                () -> SpedExportacaoApplicationService.validarIntervalo(
                        YearMonth.of(2026, 9), YearMonth.of(2026, 8)));
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
    void protegeOperacoesSpedComPermissoesDedicadas()
            throws NoSuchMethodException {
        String regraSolicitar =
                "hasAuthority('CONTABILIDADE_SPED_SOLICITAR')";
        String regraBaixar =
                "hasAuthority('CONTABILIDADE_SPED_BAIXAR')";
        String regraReprocessar =
                "hasAuthority('CONTABILIDADE_SPED_REPROCESSAR')";
        String regraCancelar =
                "hasAuthority('CONTABILIDADE_SPED_CANCELAR')";

        PreAuthorize post = SpedExportacaoController.class
                .getDeclaredMethod("solicitar",
                        SpedExportacaoController
                                .SolicitarExportacaoRequest.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize get = SpedExportacaoController.class
                .getDeclaredMethod("listar", UUID.class,
                        String.class, String.class, YearMonth.class,
                        YearMonth.class, Integer.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize prontidao = SpedExportacaoController.class
                .getDeclaredMethod("consultarProntidao")
                .getAnnotation(PreAuthorize.class);
        PreAuthorize resumo = SpedExportacaoController.class
                .getDeclaredMethod("resumir", UUID.class, String.class,
                        YearMonth.class, YearMonth.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize download = SpedExportacaoController.class
                .getDeclaredMethod("baixar", UUID.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize cancelamento = SpedExportacaoController.class
                .getDeclaredMethod("cancelar", UUID.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize reprocessamento = SpedExportacaoController.class
                .getDeclaredMethod("reprocessar", UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals(regraSolicitar, post.value());
        assertEquals(regraSolicitar, get.value());
        assertEquals(regraSolicitar, prontidao.value());
        assertEquals(regraSolicitar, resumo.value());
        assertEquals(regraBaixar, download.value());
        assertEquals(regraCancelar, cancelamento.value());
        assertEquals(regraReprocessar, reprocessamento.value());
    }
}
