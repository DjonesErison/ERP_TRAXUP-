package com.traxup.tplug.erp.trial;

import com.traxup.tplug.erp.empresa.*;
import com.traxup.tplug.erp.auth.AuthApplicationService;
import com.traxup.tplug.erp.tenant.*;
import com.traxup.tplug.erp.trial.api.*;
import com.traxup.tplug.erp.usuario.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrialProvisioningServiceTest {
    @Test
    void repeticaoDaChaveNaoCriaNovoTenant() {
        TenantRepository tenants = mock(TenantRepository.class);
        EmpresaRepository empresas = mock(EmpresaRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        TrialSaasRepository trials = mock(TrialSaasRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        AuthApplicationService auth = mock(AuthApplicationService.class);
        when(encoder.encode(any())).thenReturn("hash");
        when(auth.criarAtivacaoAdministrador(any())).thenReturn("activation-token");
        when(tenants.save(any())).thenAnswer(i -> i.getArgument(0));
        when(empresas.save(any())).thenAnswer(i -> i.getArgument(0));
        when(usuarios.save(any())).thenAnswer(i -> i.getArgument(0));
        when(trials.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(trials.findByIdempotencyKey("req-1")).thenReturn(Optional.empty());

        var service = new TrialProvisioningService(tenants, empresas, usuarios, trials, encoder, auth, mock(com.traxup.tplug.erp.trial.mail.TrialEmailQueue.class), true);
        var request = new TrialCadastroRequest("Ana", "Loja", "Loja LTDA", "12345678000199",
                "87999999999", "ana@loja.com", "Varejo", 1, true, "2026-09", "req-1");
        service.provisionar(request);

        verify(tenants, times(1)).save(any());
        verify(trials, times(1)).saveAndFlush(any());
    }
}
