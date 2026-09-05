package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaFinanceiraApplicationServiceTest {
    @Mock ContaFinanceiraRepository repository;
    @Mock ContaFinanceiraMovimentoRepository movimentoRepository;
    @Mock FilialRepository filialRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveBuscarContaSempreNoTenantInformado() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.empty());

        ContaFinanceiraApplicationService service = new ContaFinanceiraApplicationService(
                repository, movimentoRepository, filialRepository, auditoria);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(tenantId, contaId));
        verify(repository).findByIdAndTenantId(contaId, tenantId);
    }
}
