package com.traxup.tplug.erp.empresa.api;

import com.traxup.tplug.erp.empresa.Empresa;
import com.traxup.tplug.erp.empresa.EmpresaApplicationService;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmpresaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private EmpresaApplicationService empresaApplicationService;

    @Test
    void deveCriarEmpresaNoTenantDoJwt() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant API"));

        mockMvc.perform(post("/api/v1/empresas")
                        .with(jwt().jwt(token -> token.claim("tenant_id", tenant.getId().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "razaoSocial": "TRAXUP Tecnologia Ltda",
                                  "nomeFantasia": "TRAXUP",
                                  "cnpj": "12345678000199"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.tenantId").value(tenant.getId().toString()))
                .andExpect(jsonPath("$.razaoSocial").value("TRAXUP Tecnologia Ltda"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void naoDeveBuscarEmpresaDeOutroTenantMesmoComHeaderForjado() throws Exception {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant A"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant B"));
        Empresa empresaA = empresaApplicationService.criar(
                tenantA.getId(),
                "Empresa Tenant A",
                "Empresa A",
                "11111111000111");

        mockMvc.perform(get("/api/v1/empresas/{empresaId}", empresaA.getId())
                        .with(jwt().jwt(token -> token.claim("tenant_id", tenantB.getId().toString())))
                        .header("X-Tenant-Id", tenantA.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso nao encontrado"));
    }
}
