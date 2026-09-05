package com.traxup.tplug.erp.pessoa.contato.api;

import com.traxup.tplug.erp.pessoa.Pessoa;
import com.traxup.tplug.erp.pessoa.PessoaApplicationService;
import com.traxup.tplug.erp.pessoa.contato.PessoaContatoRepository;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PessoaContatoControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired TenantRepository tenantRepository;
    @Autowired PessoaApplicationService pessoaService;
    @Autowired PessoaContatoRepository contatoRepository;

    @Test
    void deveCadastrarMinimoDoisContatosComUmPrincipal() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Contatos"));
        Pessoa pessoa = criarPessoa(tenant, "Cliente Contatos");

        mockMvc.perform(post("/api/v1/pessoas/{pessoaId}/contatos/cadastro", pessoa.getId())
                        .with(jwtComTenantEPermissao(tenant, "PESSOA_CONTATO_GERENCIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contatos": [
                                    {"nome":"Maria","email":"maria@teste.com","principal":true},
                                    {"nome":"Joao","telefone":"81999990000","principal":false}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));

        assertEquals(2, contatoRepository.countByTenantIdAndPessoaId(tenant.getId(), pessoa.getId()));
        assertTrue(contatoRepository.findByTenantIdAndPessoaIdAndPrincipalTrue(
                tenant.getId(), pessoa.getId()).isPresent());
    }

    @Test
    void deveTrocarContatoPrincipalSemCriarDuplicidade() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Troca Principal"));
        Pessoa pessoa = criarPessoa(tenant, "Cliente Troca");
        cadastrarDoisContatos(tenant, pessoa);

        mockMvc.perform(post("/api/v1/pessoas/{pessoaId}/contatos", pessoa.getId())
                        .with(jwtComTenantEPermissao(tenant, "PESSOA_CONTATO_GERENCIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Novo Principal","email":"novo@teste.com","principal":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.principal").value(true));

        long principais = contatoRepository
                .findAllByTenantIdAndPessoaIdOrderByPrincipalDescNomeAsc(tenant.getId(), pessoa.getId())
                .stream().filter(contato -> contato.isPrincipal()).count();
        assertEquals(1, principais);
    }

    @Test
    void deveRejeitarCadastroInicialComMenosDeDoisContatos() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Minimo"));
        Pessoa pessoa = criarPessoa(tenant, "Cliente Minimo");

        mockMvc.perform(post("/api/v1/pessoas/{pessoaId}/contatos/cadastro", pessoa.getId())
                        .with(jwtComTenantEPermissao(tenant, "PESSOA_CONTATO_GERENCIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contatos":[{"nome":"Unico","email":"unico@teste.com","principal":true}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Regra de negocio violada"));
    }

    @Test
    void deveRejeitarContatoSemEmailETelefoneComoBadRequest() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Canal"));
        Pessoa pessoa = criarPessoa(tenant, "Cliente Canal");

        mockMvc.perform(post("/api/v1/pessoas/{pessoaId}/contatos/cadastro", pessoa.getId())
                        .with(jwtComTenantEPermissao(tenant, "PESSOA_CONTATO_GERENCIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contatos": [
                                    {"nome":"Sem Canal","principal":true},
                                    {"nome":"Valido","telefone":"81999990000","principal":false}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Contato deve possuir email ou telefone"));
    }

    @Test
    void deveNegarGerenciamentoSemPermissao() throws Exception {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant Sem Permissao"));
        Pessoa pessoa = criarPessoa(tenant, "Cliente Bloqueado");

        mockMvc.perform(post("/api/v1/pessoas/{pessoaId}/contatos/cadastro", pessoa.getId())
                        .with(jwt().jwt(token -> token.claim("tenant_id", tenant.getId().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contatos": [
                                    {"nome":"Maria","email":"maria@teste.com","principal":true},
                                    {"nome":"Joao","telefone":"81999990000","principal":false}
                                  ]
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void naoDeveListarContatosDePessoaDeOutroTenant() throws Exception {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant A Contatos"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant B Contatos"));
        Pessoa pessoaA = criarPessoa(tenantA, "Cliente Tenant A");

        mockMvc.perform(get("/api/v1/pessoas/{pessoaId}/contatos", pessoaA.getId())
                        .with(jwtComTenantEPermissao(tenantB, "PESSOA_CONTATO_LER")))
                .andExpect(status().isNotFound());
    }

    private void cadastrarDoisContatos(Tenant tenant, Pessoa pessoa) throws Exception {
        mockMvc.perform(post("/api/v1/pessoas/{pessoaId}/contatos/cadastro", pessoa.getId())
                        .with(jwtComTenantEPermissao(tenant, "PESSOA_CONTATO_GERENCIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contatos": [
                                    {"nome":"Principal","email":"principal@teste.com","principal":true},
                                    {"nome":"Secundario","telefone":"81999990000","principal":false}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated());
    }

    private Pessoa criarPessoa(Tenant tenant, String nome) {
        return pessoaService.criar(tenant.getId(), "JURIDICA", nome, null,
                null, null, null, true, false);
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor jwtComTenantEPermissao(
            Tenant tenant, String permissao) {
        return jwt()
                .jwt(token -> token.claim("tenant_id", tenant.getId().toString()))
                .authorities(new SimpleGrantedAuthority(permissao));
    }
}
