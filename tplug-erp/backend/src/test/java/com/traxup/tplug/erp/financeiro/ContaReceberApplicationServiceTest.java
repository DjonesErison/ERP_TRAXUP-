package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
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
class ContaReceberApplicationServiceTest {
    @Mock ContaReceberRepository repository;
    @Mock FilialRepository filialRepository;
    @Mock PessoaRepository pessoaRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveBuscarContaSempreNoTenantInformado() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.empty());

        ContaReceberApplicationService service = new ContaReceberApplicationService(
                repository, filialRepository, pessoaRepository, auditoria);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(tenantId, contaId));
        verify(repository).findByIdAndTenantId(contaId, tenantId);
    }
}
