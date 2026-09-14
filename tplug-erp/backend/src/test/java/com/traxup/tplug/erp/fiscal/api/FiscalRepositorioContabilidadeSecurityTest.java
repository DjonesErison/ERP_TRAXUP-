package com.traxup.tplug.erp.fiscal.api;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiscalRepositorioContabilidadeSecurityTest {
    private static final String REGRA =
            "hasAuthority('FISCAL_REPOSITORIO_CONTABILIDADE_LER')";

    @Test
    void protegeConsultaDownloadEExportacaoComPermissaoDedicada()
            throws NoSuchMethodException {
        assertEquals(REGRA, regra(FiscalRepositorioConsultaController.class,
                "listar", LocalDate.class, LocalDate.class,
                String.class, Integer.class));
        assertEquals(REGRA, regra(FiscalArquivoDownloadController.class,
                "baixar", UUID.class));
        assertEquals(REGRA, regra(FiscalExportacaoContabilidadeController.class,
                "exportar", LocalDate.class, LocalDate.class, int.class));
    }

    private String regra(Class<?> controller, String metodo,
                         Class<?>... parametros) throws NoSuchMethodException {
        PreAuthorize anotacao = controller
                .getDeclaredMethod(metodo, parametros)
                .getAnnotation(PreAuthorize.class);
        return anotacao.value();
    }
}
