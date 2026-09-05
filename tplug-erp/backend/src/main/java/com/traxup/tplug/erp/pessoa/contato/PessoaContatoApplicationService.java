package com.traxup.tplug.erp.pessoa.contato;

import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PessoaContatoApplicationService {

    public static final int MINIMO_CONTATOS = 2;

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

    public SituacaoContatos situacao(UUID tenantId, UUID pessoaId) {
        validarPessoa(tenantId, pessoaId);
        long quantidade = repository.countByTenantIdAndPessoaId(tenantId, pessoaId);
        boolean possuiPrincipal = repository.findByTenantIdAndPessoaIdAndPrincipalTrue(tenantId, pessoaId).isPresent();
        return new SituacaoContatos(quantidade, MINIMO_CONTATOS, possuiPrincipal,
                quantidade >= MINIMO_CONTATOS && possuiPrincipal);
    }

    @Transactional
    public PessoaContato criar(UUID tenantId, UUID pessoaId, String nome, String cargo,
                               String email, String telefone, boolean principal) {
        validarPessoaParaAlteracao(tenantId, pessoaId);
        if (repository.countByTenantIdAndPessoaId(tenantId, pessoaId) < MINIMO_CONTATOS) {
            throw new RegraNegocioException(
                    "Cadastro incompleto: use o endpoint de cadastro em lote para atingir no minimo dois contatos");
        }
        return salvarContato(tenantId, pessoaId, new NovoContato(nome, cargo, email, telefone, principal));
    }

    @Transactional
    public List<PessoaContato> cadastrar(UUID tenantId, UUID pessoaId, List<NovoContato> novosContatos) {
        validarPessoaParaAlteracao(tenantId, pessoaId);
        if (novosContatos == null || novosContatos.isEmpty()) {
            throw new RegraNegocioException("Informe ao menos um contato");
        }

        long quantidadeAtual = repository.countByTenantIdAndPessoaId(tenantId, pessoaId);
        if (quantidadeAtual + novosContatos.size() < MINIMO_CONTATOS) {
            throw new RegraNegocioException("Cada cliente ou fornecedor deve possuir no minimo dois contatos");
        }

        long principaisInformados = novosContatos.stream().filter(NovoContato::principal).count();
        if (principaisInformados > 1) {
            throw new RegraNegocioException("Apenas um contato pode ser definido como principal");
        }

        Optional<PessoaContato> principalAtual =
                repository.findByTenantIdAndPessoaIdAndPrincipalTrue(tenantId, pessoaId);
        if (principaisInformados == 0 && principalAtual.isEmpty()) {
            throw new RegraNegocioException("Um dos contatos deve ser definido como principal");
        }

        validarContatos(novosContatos);
        if (principaisInformados == 1) {
            principalAtual.ifPresent(PessoaContato::desmarcarPrincipal);
        }

        List<PessoaContato> contatos = novosContatos.stream()
                .map(contato -> novoContato(tenantId, pessoaId, contato))
                .toList();
        return repository.saveAll(contatos);
    }

    private PessoaContato salvarContato(UUID tenantId, UUID pessoaId, NovoContato contato) {
        validarContato(contato);
        if (contato.principal()) {
            repository.findByTenantIdAndPessoaIdAndPrincipalTrue(tenantId, pessoaId)
                    .ifPresent(PessoaContato::desmarcarPrincipal);
        } else if (repository.findByTenantIdAndPessoaIdAndPrincipalTrue(tenantId, pessoaId).isEmpty()) {
            throw new RegraNegocioException("Um dos contatos deve permanecer definido como principal");
        }
        return repository.save(novoContato(tenantId, pessoaId, contato));
    }

    private PessoaContato novoContato(UUID tenantId, UUID pessoaId, NovoContato contato) {
        return new PessoaContato(tenantId, pessoaId, contato.nome().trim(), normalizar(contato.cargo()),
                normalizar(contato.email()), normalizar(contato.telefone()), contato.principal());
    }

    private void validarContatos(List<NovoContato> contatos) {
        contatos.forEach(this::validarContato);
    }

    private void validarContato(NovoContato contato) {
        if (contato == null || contato.nome() == null || contato.nome().isBlank()) {
            throw new RegraNegocioException("Nome do contato e obrigatorio");
        }
        if ((contato.email() == null || contato.email().isBlank())
                && (contato.telefone() == null || contato.telefone().isBlank())) {
            throw new RegraNegocioException("Contato deve possuir email ou telefone");
        }
    }

    private void validarPessoa(UUID tenantId, UUID pessoaId) {
        pessoaRepository.findByIdAndTenantId(pessoaId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pessoa nao encontrada para o tenant informado"));
    }

    private void validarPessoaParaAlteracao(UUID tenantId, UUID pessoaId) {
        pessoaRepository.findByIdAndTenantIdForUpdate(pessoaId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pessoa nao encontrada para o tenant informado"));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public record NovoContato(
            String nome, String cargo, String email, String telefone, boolean principal) {}

    public record SituacaoContatos(
            long quantidade, int minimoObrigatorio, boolean possuiPrincipal, boolean cadastroCompleto) {}
}
