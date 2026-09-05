package com.traxup.tplug.erp.pessoa.endereco;

import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PessoaEnderecoApplicationService {

    private static final Set<String> TIPOS = Set.of("PRINCIPAL", "COBRANCA", "ENTREGA", "OUTRO");

    private final PessoaEnderecoRepository enderecoRepository;
    private final PessoaRepository pessoaRepository;

    public PessoaEnderecoApplicationService(PessoaEnderecoRepository enderecoRepository, PessoaRepository pessoaRepository) {
        this.enderecoRepository = enderecoRepository;
        this.pessoaRepository = pessoaRepository;
    }

    public List<PessoaEndereco> listar(UUID tenantId, UUID pessoaId) {
        validarPessoa(tenantId, pessoaId);
        return enderecoRepository.findAllByTenantIdAndPessoaIdOrderByPrincipalDescCriadoEmAsc(tenantId, pessoaId);
    }

    @Transactional
    public PessoaEndereco criar(UUID tenantId, UUID pessoaId, String tipo, String logradouro, String numero,
                                 String complemento, String bairro, String cidade, String uf, String cep, boolean principal) {
        validarPessoa(tenantId, pessoaId);
        String tipoNormalizado = tipo.toUpperCase();
        if (!TIPOS.contains(tipoNormalizado)) throw new IllegalArgumentException("Tipo de endereco invalido");
        String ufNormalizada = uf.trim().toUpperCase();
        if (ufNormalizada.length() != 2) throw new IllegalArgumentException("UF deve possuir 2 caracteres");
        String cepNormalizado = normalizarCep(cep);
        return enderecoRepository.save(new PessoaEndereco(
                tenantId, pessoaId, tipoNormalizado, logradouro.trim(), normalizar(numero), normalizar(complemento),
                normalizar(bairro), cidade.trim(), ufNormalizada, cepNormalizado, principal));
    }

    private void validarPessoa(UUID tenantId, UUID pessoaId) {
        pessoaRepository.findByIdAndTenantId(pessoaId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa nao encontrada para o tenant informado"));
    }

    private String normalizarCep(String cep) {
        if (cep == null || cep.isBlank()) return null;
        String valor = cep.replaceAll("\\D", "");
        if (valor.length() != 8) throw new IllegalArgumentException("CEP deve possuir 8 digitos");
        return valor;
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
