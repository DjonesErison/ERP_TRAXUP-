package com.traxup.tplug.erp.filial.api;

import com.traxup.tplug.erp.empresa.Empresa;
import com.traxup.tplug.erp.empresa.EmpresaApplicationService;
import com.traxup.tplug.erp.filial.Filial;
import com.traxup.tplug.erp.filial.FilialApplicationService;
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
class FilialControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private EmpresaApplicationService empresaApplicationService;

    @Autowired
    private FilialApplicationService filialApplicationService;

    @Test
    void deveCriarFilialNaEmpresaDoTenantDoJwt() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Filial API"));
        Empresa empresa = empresaApplicationService.criar(
                tenant.getId(), "Empresa Matriz Ltda", "Matriz", "22222222000122");

        mockMvc.perform(post("/api/v1/filiais")
                        .with(jwt().jwt(token -> token.claim("tenant_id", tenant.getId().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "empresaId": "%s",
                                  "nome": "Filial Centro",
                                  "cnpj": "22222222000203"
                                }
                                """.formatted(empresa.getId())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.tenantId").value(tenant.getId().toString()))
                .andExpect(jsonPath("$.empresaId").value(empresa.getId().toString()))
                .andExpect(jsonPath("$.nome").value("Filial Centro"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void naoDeveCriarFilialUsandoEmpresaDeOutroTenant() throws Exception {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant A Filial"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant B Filial"));
        Empresa empresaA = empresaApplicationService.criar(
                tenantA.getId(), "Empresa A Ltda", "Empresa A", "33333333000133");

        mockMvc.perform(post("/api/v1/filiais")
                        .with(jwt().jwt(token -> token.claim("tenant_id", tenantB.getId().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "empresaId": "%s",
                                  "nome": "Filial Indevida",
                                  "cnpj": "44444444000144"
                                }
                                """.formatted(empresaA.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso nao encontrado"));
    }

    @Test
    void naoDeveBuscarFilialDeOutroTenant() throws Exception {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant A Busca Filial"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant B Busca Filial"));
        Empresa empresaA = empresaApplicationService.criar(
                tenantA.getId(), "Empresa Busca A Ltda", "Busca A", "55555555000155");
        Filial filialA = filialApplicationService.criar(
                tenantA.getId(), empresaA.getId(), "Filial A", "55555555000236");

        mockMvc.perform(get("/api/v1/filiais/{filialId}", filialA.getId())
                        .with(jwt().jwt(token -> token.claim("tenant_id", tenantB.getId().toString()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso nao encontrado"));
    }
}
