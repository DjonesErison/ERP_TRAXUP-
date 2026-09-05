package com.traxup.tplug.erp.auditoria.api;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuditoriaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private AuditoriaApplicationService auditoriaApplicationService;

    @Test
    void deveListarSomenteAuditoriaDoTenantDoJwtComPermissao() throws Exception {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant Auditoria A"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant Auditoria B"));

        UUID entidadeA = UUID.randomUUID();
        UUID entidadeB = UUID.randomUUID();

        auditoriaApplicationService.registrar(
                tenantA.getId(), null, null, null,
                "CRIAR", "EMPRESA", entidadeA, "Evento A");
        auditoriaApplicationService.registrar(
                tenantB.getId(), null, null, null,
                "CRIAR", "EMPRESA", entidadeB, "Evento B");

        mockMvc.perform(get("/api/v1/auditorias")
                        .with(jwt()
                                .jwt(token -> token.claim("tenant_id", tenantA.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("AUDITORIA_LER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tenantId").value(tenantA.getId().toString()))
                .andExpect(jsonPath("$[0].entidadeId").value(entidadeA.toString()))
                .andExpect(jsonPath("$[?(@.tenantId == '%s')]", tenantB.getId().toString()).doesNotExist());
    }

    @Test
    void deveRetornarForbiddenSemPermissao() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Auditoria Sem Permissao"));

        mockMvc.perform(get("/api/v1/auditorias")
                        .with(jwt().jwt(token -> token.claim("tenant_id", tenant.getId().toString()))))
                .andExpect(status().isForbidden());
    }
}
