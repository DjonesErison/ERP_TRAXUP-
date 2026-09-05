package com.traxup.tplug.erp.pessoa.contato;

import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PessoaContatoApplicationService {

    private final PessoaContatoRepository repository;
    private final PessoaRepository pessoaRepository;

    public PessoaContatoApplicationService(PessoaContatoRepository repository, PessoaRepository pessoaRepository) {
        this.repository = repository;
        this.pessoaRepository = pessoaRepository;
    }

    public List<PessoaContato> listar(UUID tenantId, UUID pessoaId) {
        validarPessoa(tenantId, pessoaId);
        return repository.findAllByTenantIdAndPessoaIdOrderByPrincipalDescNomeAsc(tenantId, pessoaId);
    }

    @Transactional
    public PessoaContato criar(UUID tenantId, UUID pessoaId, String nome, String cargo, String email, String telefone, boolean principal) {
        validarPessoa(tenantId, pessoaId);
        if ((email == null || email.isBlank()) && (telefone == null || telefone.isBlank())) {
            throw new IllegalArgumentException("Contato deve possuir email ou telefone");
        }
        return repository.save(new PessoaContato(tenantId, pessoaId, nome.trim(), normalizar(cargo), normalizar(email), normalizar(telefone), principal));
    }

    private void validarPessoa(UUID tenantId, UUID pessoaId) {
        pessoaRepository.findByIdAndTenantId(pessoaId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa nao encontrada para o tenant informado"));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
