package com.traxup.tplug.erp.pessoa;

import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PessoaApplicationService {

    private static final Set<String> TIPOS = Set.of("FISICA", "JURIDICA");

    private final PessoaRepository pessoaRepository;
    private final TenantRepository tenantRepository;

    public PessoaApplicationService(PessoaRepository pessoaRepository, TenantRepository tenantRepository) {
        this.pessoaRepository = pessoaRepository;
        this.tenantRepository = tenantRepository;
    }

    public List<Pessoa> listar(UUID tenantId, String papel) {
        validarTenant(tenantId);
        if (papel == null || papel.isBlank()) return pessoaRepository.findAllByTenantIdOrderByNomeRazaoSocialAsc(tenantId);
        return switch (papel.toUpperCase()) {
            case "CLIENTE" -> pessoaRepository.findAllByTenantIdAndClienteTrueOrderByNomeRazaoSocialAsc(tenantId);
            case "FORNECEDOR" -> pessoaRepository.findAllByTenantIdAndFornecedorTrueOrderByNomeRazaoSocialAsc(tenantId);
            default -> throw new IllegalArgumentException("Papel invalido");
        };
    }

    public Pessoa buscarPorId(UUID tenantId, UUID pessoaId) {
        return pessoaRepository.findByIdAndTenantId(pessoaId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa nao encontrada para o tenant informado"));
    }

    @Transactional
    public Pessoa criar(UUID tenantId, String tipoPessoa, String nomeRazaoSocial, String nomeFantasia,
                        String cpfCnpj, String email, String telefone, boolean cliente, boolean fornecedor) {
        Tenant tenant = validarTenant(tenantId);
        String tipo = tipoPessoa.toUpperCase();
        if (!TIPOS.contains(tipo)) throw new IllegalArgumentException("Tipo de pessoa invalido");
        if (!cliente && !fornecedor) throw new IllegalArgumentException("Pessoa deve ser cliente, fornecedor ou ambos");
        String documento = limparDocumento(cpfCnpj);
        if (documento != null && pessoaRepository.existsByTenantIdAndCpfCnpj(tenantId, documento)) {
            throw new RecursoConflitanteException("Ja existe pessoa com este CPF/CNPJ no tenant");
        }
        Pessoa pessoa = new Pessoa(tenant, tipo, nomeRazaoSocial.trim(), normalizar(nomeFantasia), documento,
                normalizar(email), normalizar(telefone), cliente, fornecedor);
        return pessoaRepository.save(pessoa);
    }

    @Transactional
    public Pessoa desativar(UUID tenantId, UUID pessoaId) {
        Pessoa pessoa = buscarPorId(tenantId, pessoaId);
        pessoa.desativar();
        return pessoaRepository.save(pessoa);
    }

    private Tenant validarTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tenant nao encontrado"));
    }

    private String limparDocumento(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String documento = valor.replaceAll("\\D", "");
        if (documento.length() != 11 && documento.length() != 14) {
            throw new IllegalArgumentException("CPF/CNPJ deve possuir 11 ou 14 digitos");
        }
        return documento;
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
