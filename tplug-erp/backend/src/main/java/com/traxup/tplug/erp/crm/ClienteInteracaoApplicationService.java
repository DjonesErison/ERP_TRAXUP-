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
public class ClienteInteracaoApplicationService {
    private static final Set<String> CANAIS = Set.of("TELEFONE", "EMAIL", "WHATSAPP", "PRESENCIAL", "OUTRO");
    private static final Set<String> RESULTADOS = Set.of("CONTATO_REALIZADO", "SEM_RETORNO", "INTERESSE", "SEM_INTERESSE", "OUTRO");
    private static final int LIMITE_PADRAO = 100;
    private static final int LIMITE_MAXIMO = 500;

    private final ClienteInteracaoRepository repository;
    private final ClienteFollowUpRepository followUpRepository;
    private final PessoaRepository pessoaRepository;
    private final FilialRepository filialRepository;
    private final AuditoriaApplicationService auditoria;

    public ClienteInteracaoApplicationService(ClienteInteracaoRepository repository,
                                               ClienteFollowUpRepository followUpRepository,
                                               PessoaRepository pessoaRepository,
                                               FilialRepository filialRepository,
                                               AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.followUpRepository = followUpRepository;
        this.pessoaRepository = pessoaRepository;
        this.filialRepository = filialRepository;
        this.auditoria = auditoria;
    }

    public List<ClienteInteracao> listar(UUID tenantId, UUID filialId, UUID clienteId, String canal,
                                         String resultado, Instant inicio, Instant fim, Integer limite) {
        int tamanho = limite == null ? LIMITE_PADRAO : limite;
        if (tamanho < 1 || tamanho > LIMITE_MAXIMO) throw new RegraNegocioException("Limite deve estar entre 1 e 500");
        if (inicio != null && fim != null && inicio.isAfter(fim)) throw new RegraNegocioException("Periodo de interacoes invalido");
        validarFilial(tenantId, filialId, false);
        if (clienteId != null) validarCliente(tenantId, clienteId);
        String canalNormalizado = normalizarOpcao(canal, CANAIS, "Canal de interacao invalido");
        String resultadoNormalizado = normalizarOpcao(resultado, RESULTADOS, "Resultado de interacao invalido");
        return repository.buscar(tenantId, filialId, clienteId, canalNormalizado, resultadoNormalizado,
                inicio, fim, PageRequest.of(0, tamanho));
    }

    public ClienteInteracao buscar(UUID tenantId, UUID interacaoId) {
        return repository.findByIdAndTenantId(interacaoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Interacao de cliente nao encontrada para o tenant informado"));
    }

    @Transactional
    public ClienteInteracao registrar(UUID tenantId, UUID usuarioId, UUID filialId, UUID clienteId, UUID followUpId,
                                      String canal, String resultado, String assunto, Instant ocorridoEm) {
        validarFilial(tenantId, filialId, true);
        validarCliente(tenantId, clienteId);
        if (ocorridoEm == null) throw new RegraNegocioException("Data da interacao e obrigatoria");
        if (ocorridoEm.isAfter(Instant.now().plusSeconds(300))) throw new RegraNegocioException("Data da interacao nao pode estar no futuro");
        String canalNormalizado = normalizarObrigatorio(canal, CANAIS, "Canal de interacao invalido");
        String resultadoNormalizado = normalizarObrigatorio(resultado, RESULTADOS, "Resultado de interacao invalido");
        String assuntoNormalizado = normalizarAssunto(assunto);
        validarFollowUp(tenantId, filialId, clienteId, followUpId);

        ClienteInteracao interacao = repository.save(new ClienteInteracao(
                tenantId, filialId, clienteId, followUpId, canalNormalizado, resultadoNormalizado,
                assuntoNormalizado, ocorridoEm, usuarioId));
        auditoria.registrar(tenantId, usuarioId, null, filialId, "REGISTRAR", "CRM_CLIENTE_INTERACAO",
                interacao.getId(), "clienteId=" + clienteId + ";canal=" + canalNormalizado + ";resultado=" + resultadoNormalizado);
        return interacao;
    }

    private void validarFilial(UUID tenantId, UUID filialId, boolean obrigatoria) {
        if (filialId == null) {
            if (obrigatoria) throw new RegraNegocioException("Filial da interacao e obrigatoria");
            return;
        }
        if (!filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }
    }

    private Pessoa validarCliente(UUID tenantId, UUID clienteId) {
        Pessoa cliente = pessoaRepository.findByIdAndTenantId(clienteId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado para o tenant informado"));
        if (!cliente.isCliente()) throw new RegraNegocioException("Pessoa informada precisa ser cliente");
        return cliente;
    }

    private void validarFollowUp(UUID tenantId, UUID filialId, UUID clienteId, UUID followUpId) {
        if (followUpId == null) return;
        ClienteFollowUp followUp = followUpRepository.findByIdAndTenantId(followUpId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Follow-up nao encontrado para o tenant informado"));
        if (!followUp.getFilialId().equals(filialId) || !followUp.getClienteId().equals(clienteId)) {
            throw new RegraNegocioException("Follow-up precisa pertencer ao mesmo cliente e filial da interacao");
        }
    }

    private String normalizarOpcao(String valor, Set<String> permitidos, String mensagem) {
        if (valor == null || valor.isBlank()) return null;
        String normalizado = valor.trim().toUpperCase();
        if (!permitidos.contains(normalizado)) throw new RegraNegocioException(mensagem);
        return normalizado;
    }

    private String normalizarObrigatorio(String valor, Set<String> permitidos, String mensagem) {
        String normalizado = normalizarOpcao(valor, permitidos, mensagem);
        if (normalizado == null) throw new RegraNegocioException(mensagem);
        return normalizado;
    }

    private String normalizarAssunto(String assunto) {
        if (assunto == null || assunto.isBlank()) throw new RegraNegocioException("Assunto da interacao e obrigatorio");
        String normalizado = assunto.trim();
        if (normalizado.length() > 160) throw new RegraNegocioException("Assunto da interacao deve possuir no maximo 160 caracteres");
        return normalizado;
    }
}
