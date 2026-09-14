package com.traxup.tplug.erp.auth;

import com.traxup.tplug.erp.auth.api.RbacFilialAcessoController;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilialAcessoApplicationServiceTest {
    @Test
    void normalizaFiliaisComOrdemDeterministica() {
        UUID maior = UUID.fromString(
                "ffffffff-ffff-ffff-ffff-ffffffffffff");
        UUID menor = UUID.fromString(
                "00000000-0000-0000-0000-000000000001");

        assertEquals(List.of(menor, maior),
                FilialAcessoApplicationService
                        .normalizarFiliais(List.of(maior, menor)));
    }

    @Test
    void rejeitaListaNulaDuplicadaOuAcimaDoLimite() {
        UUID filialId = UUID.randomUUID();

        assertThrows(RegraNegocioException.class,
                () -> FilialAcessoApplicationService
                        .normalizarFiliais(null));
        assertThrows(RegraNegocioException.class,
                () -> FilialAcessoApplicationService
                        .normalizarFiliais(List.of(filialId, filialId)));
        assertThrows(RegraNegocioException.class,
                () -> FilialAcessoApplicationService.normalizarFiliais(
                        java.util.stream.IntStream.range(0, 201)
                                .mapToObj(i -> UUID.randomUUID()).toList()));
    }

    @Test
    void gestaoDeFiliaisExigePermissaoRbac() {
        PreAuthorize regra = RbacFilialAcessoController.class
                .getAnnotation(PreAuthorize.class);

        assertEquals("hasAuthority('RBAC_GERENCIAR')", regra.value());
    }
}
