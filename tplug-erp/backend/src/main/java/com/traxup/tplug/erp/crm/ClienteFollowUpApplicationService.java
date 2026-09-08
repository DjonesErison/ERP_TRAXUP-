package com.traxup.tplug.erp.crm;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.Pessoa;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ClienteFollowUpApplicationService {
    private static final Set<String> STATUS = Set.of("PENDENTE", "CONCLUIDO", "CANCELADO");
    private static final int LIMITE_PADRAO = 100;
    private static final int LIMITE_MAXIMO = 500;

    private final ClienteFollowUpRepository repository;
    private final PessoaRepository pessoaRepository;
    private final FilialRepository filialRepository;
    private final AuditoriaApplicationService auditoria;

    public ClienteFollowUpApplicationService(ClienteFollowUpRepository repository,
                                             PessoaRepository pessoaRepository,
                                             FilialRepository filialRepository,
                                             AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.pessoaRepository = pessoaRepository;
        this.filialRepository = filialRepository;
        this.auditoria = auditoria;
    }

    public List<ClienteFollowUp> listar(UUID tenantId, UUID filialId, UUID clienteId, String status, Integer limite) {
        int tamanho = limite == null ? LIMITE_PADRAO : limite;
        if (tamanho < 1 || tamanho > LIMITE_MAXIMO) {
            throw new RegraNegocioException("Limite deve estar entre 1 e 500");
        }
        String statusNormalizado = normalizarStatus(status);
        validarFilial(tenantId, filialId);
        if (clienteId != null) validarCliente(tenantId, clienteId);
        return repository.buscar(tenantId, filialId, clienteId, statusNormalizado, PageRequest.of(0, tamanho));
    }

    public ClienteFollowUp buscar(UUID tenantId, UUID followUpId) {
        return repository.findByIdAndTenantId(followUpId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Follow-up de cliente nao encontrado para o tenant informado"));
    }

    @Transactional
    public ClienteFollowUp criar(UUID tenantId, UUID usuarioId, UUID filialId, UUID clienteId,
                                  String assunto, String observacao, Instant agendadoPara) {
        validarFilialObrigatoria(tenantId, filialId);
        validarCliente(tenantId, clienteId);
        if (agendadoPara == null) throw new RegraNegocioException("Data do follow-up e obrigatoria");
        String assuntoNormalizado = normalizarObrigatorio(assunto, "Assunto do follow-up e obrigatorio", 160);
        String observacaoNormalizada = normalizarOpcional(observacao, 500, "Observacao do follow-up deve possuir no maximo 500 caracteres");
        ClienteFollowUp followUp = repository.save(new ClienteFollowUp(
                tenantId, filialId, clienteId, assuntoNormalizado, observacaoNormalizada, agendadoPara, usuarioId));
        auditoria.registrar(tenantId, usuarioId, null, filialId, "CRIAR", "CRM_CLIENTE_FOLLOWUP",
                followUp.getId(), "clienteId=" + clienteId);
        return followUp;
    }

    @Transactional
    public ClienteFollowUp concluir(UUID tenantId, UUID usuarioId, UUID followUpId) {
        ClienteFollowUp followUp = buscarParaAtualizar(tenantId, followUpId);
        followUp.concluir(usuarioId);
        repository.save(followUp);
        auditoria.registrar(tenantId, usuarioId, null, followUp.getFilialId(), "CONCLUIR", "CRM_CLIENTE_FOLLOWUP",
                followUp.getId(), "clienteId=" + followUp.getClienteId());
        return followUp;
    }

    @Transactional
    public ClienteFollowUp cancelar(UUID tenantId, UUID usuarioId, UUID followUpId) {
        ClienteFollowUp followUp = buscarParaAtualizar(tenantId, followUpId);
        followUp.cancelar(usuarioId);
        repository.save(followUp);
        auditoria.registrar(tenantId, usuarioId, null, followUp.getFilialId(), "CANCELAR", "CRM_CLIENTE_FOLLOWUP",
                followUp.getId(), "clienteId=" + followUp.getClienteId());
        return followUp;
    }

    private ClienteFollowUp buscarParaAtualizar(UUID tenantId, UUID followUpId) {
        return repository.buscarParaAtualizar(followUpId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Follow-up de cliente nao encontrado para o tenant informado"));
    }

    private void validarFilialObrigatoria(UUID tenantId, UUID filialId) {
        if (filialId == null) throw new RegraNegocioException("Filial do follow-up e obrigatoria");
        validarFilial(tenantId, filialId);
    }

    private void validarFilial(UUID tenantId, UUID filialId) {
        if (filialId != null && !filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }
    }

    private Pessoa validarCliente(UUID tenantId, UUID clienteId) {
        Pessoa cliente = pessoaRepository.findByIdAndTenantId(clienteId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado para o tenant informado"));
        if (!cliente.isCliente() || !cliente.isAtivo()) {
            throw new RegraNegocioException("Pessoa informada precisa ser um cliente ativo");
        }
        return cliente;
    }

    private String normalizarStatus(String status) {
        if (status == null || status.isBlank()) return "PENDENTE";
        String normalizado = status.trim().toUpperCase();
        if (!STATUS.contains(normalizado)) throw new RegraNegocioException("Status de follow-up invalido");
        return normalizado;
    }

    private String normalizarObrigatorio(String valor, String mensagem, int maximo) {
        if (valor == null || valor.isBlank()) throw new RegraNegocioException(mensagem);
        String normalizado = valor.trim();
        if (normalizado.length() > maximo) throw new RegraNegocioException("Assunto do follow-up deve possuir no maximo 160 caracteres");
        return normalizado;
    }

    private String normalizarOpcional(String valor, int maximo, String mensagem) {
        if (valor == null || valor.isBlank()) return null;
        String normalizado = valor.trim();
        if (normalizado.length() > maximo) throw new RegraNegocioException(mensagem);
        return normalizado;
    }
}
