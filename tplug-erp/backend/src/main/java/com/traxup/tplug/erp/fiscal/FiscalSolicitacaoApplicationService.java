package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.venda.PedidoVenda;
import com.traxup.tplug.erp.venda.PedidoVendaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FiscalSolicitacaoApplicationService {
    private static final Set<String> MODELOS = Set.of("NFCE", "NFE");
    private static final Set<String> AMBIENTES = Set.of("HOMOLOGACAO", "PRODUCAO");

    private final FiscalSolicitacaoRepository repository;
    private final PedidoVendaRepository pedidoVendaRepository;
    private final AuditoriaApplicationService auditoria;

    public FiscalSolicitacaoApplicationService(FiscalSolicitacaoRepository repository,
                                               PedidoVendaRepository pedidoVendaRepository,
                                               AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.pedidoVendaRepository = pedidoVendaRepository;
        this.auditoria = auditoria;
    }

    public List<FiscalSolicitacao> listar(UUID tenantId) {
        return repository.findAllByTenantIdOrderByCriadoEmDesc(tenantId);
    }

    public FiscalSolicitacao buscar(UUID tenantId, UUID solicitacaoId) {
        return repository.findByIdAndTenantId(solicitacaoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitacao fiscal nao encontrada para o tenant informado"));
    }

    @Transactional
    public Resultado solicitar(UUID tenantId, UUID usuarioId, UUID pedidoVendaId, String modelo, String ambiente) {
        if (pedidoVendaId == null) throw new IllegalArgumentException("Pedido de venda e obrigatorio");
        String modeloNormalizado = normalizar(modelo, MODELOS, "Modelo fiscal invalido");
        String ambienteNormalizado = normalizar(ambiente, AMBIENTES, "Ambiente fiscal invalido");

        PedidoVenda pedido = pedidoVendaRepository.findByIdAndTenantId(pedidoVendaId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de venda nao encontrado para o tenant informado"));
        if (!"FATURADO".equals(pedido.getStatus())) {
            throw new IllegalArgumentException("Somente venda FATURADA pode originar solicitacao fiscal");
        }

        var existente = repository.findByTenantIdAndPedidoVendaIdAndModeloAndAmbiente(
                tenantId, pedidoVendaId, modeloNormalizado, ambienteNormalizado);
        if (existente.isPresent()) return new Resultado(existente.get(), true);

        try {
            FiscalSolicitacao solicitacao = repository.saveAndFlush(new FiscalSolicitacao(
                    tenantId, pedido.getFilialId(), pedidoVendaId, modeloNormalizado, ambienteNormalizado));
            auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(),
                    "SOLICITAR_EMISSAO", "FISCAL_SOLICITACAO", solicitacao.getId(),
                    "pedidoVendaId=" + pedidoVendaId + ";modelo=" + modeloNormalizado + ";ambiente=" + ambienteNormalizado);
            return new Resultado(solicitacao, false);
        } catch (DataIntegrityViolationException ex) {
            FiscalSolicitacao solicitacao = repository.findByTenantIdAndPedidoVendaIdAndModeloAndAmbiente(
                    tenantId, pedidoVendaId, modeloNormalizado, ambienteNormalizado).orElseThrow(() -> ex);
            return new Resultado(solicitacao, true);
        }
    }

    @Transactional
    public ResultadoProcessamento iniciarProcessamento(UUID tenantId, UUID usuarioId, UUID solicitacaoId) {
        FiscalSolicitacao solicitacao = buscar(tenantId, solicitacaoId);
        boolean iniciada = solicitacao.iniciarProcessamento();
        if (iniciada) {
            auditoria.registrar(tenantId, usuarioId, null, solicitacao.getFilialId(),
                    "INICIAR_PROCESSAMENTO", "FISCAL_SOLICITACAO", solicitacao.getId(),
                    "pedidoVendaId=" + solicitacao.getPedidoVendaId()
                            + ";modelo=" + solicitacao.getModelo()
                            + ";ambiente=" + solicitacao.getAmbiente());
        }
        return new ResultadoProcessamento(solicitacao, !iniciada);
    }

    private String normalizar(String valor, Set<String> permitidos, String mensagem) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(mensagem);
        String normalizado = valor.trim().toUpperCase(Locale.ROOT).replace("-", "");
        if (!permitidos.contains(normalizado)) throw new IllegalArgumentException(mensagem);
        return normalizado;
    }

    public record Resultado(FiscalSolicitacao solicitacao, boolean repetida) {}
    public record ResultadoProcessamento(FiscalSolicitacao solicitacao, boolean repetida) {}
}
