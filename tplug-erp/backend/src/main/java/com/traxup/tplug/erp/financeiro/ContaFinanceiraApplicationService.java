package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ContaFinanceiraApplicationService {
    private final ContaFinanceiraRepository repository;
    private final ContaFinanceiraMovimentoRepository movimentoRepository;
    private final FilialRepository filialRepository;
    private final AuditoriaApplicationService auditoria;

    public ContaFinanceiraApplicationService(ContaFinanceiraRepository repository,
                                             ContaFinanceiraMovimentoRepository movimentoRepository,
                                             FilialRepository filialRepository,
                                             AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.movimentoRepository = movimentoRepository;
        this.filialRepository = filialRepository;
        this.auditoria = auditoria;
    }

    public List<ContaFinanceira> listar(UUID tenantId) {
        return repository.findAllByTenantIdOrderByNomeAsc(tenantId);
    }

    public ContaFinanceira buscar(UUID tenantId, UUID contaId) {
        return repository.findByIdAndTenantId(contaId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta financeira nao encontrada para o tenant informado"));
    }

    public List<ContaFinanceiraMovimento> listarMovimentos(UUID tenantId, UUID contaId) {
        buscar(tenantId, contaId);
        return movimentoRepository.findAllByTenantIdAndContaFinanceiraIdOrderByOcorridoEmDesc(tenantId, contaId);
    }

    @Transactional
    public ContaFinanceira criar(UUID tenantId, UUID usuarioId, UUID filialId, String nome, String tipo) {
        if (!filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }
        String nomeNormalizado = normalizarObrigatorio(nome, "Nome");
        String tipoNormalizado = normalizarTipoConta(tipo);
        if (repository.existsByTenantIdAndFilialIdAndNomeIgnoreCase(tenantId, filialId, nomeNormalizado)) {
            throw new IllegalArgumentException("Ja existe conta financeira com este nome na filial");
        }
        ContaFinanceira conta = repository.save(new ContaFinanceira(
                tenantId, filialId, nomeNormalizado, tipoNormalizado, usuarioId));
        auditoria.registrar(tenantId, usuarioId, null, filialId,
                "CRIAR", "CONTA_FINANCEIRA", conta.getId(), "tipo=" + tipoNormalizado);
        return conta;
    }

    @Transactional
    public ContaFinanceira movimentar(UUID tenantId, UUID usuarioId, UUID contaId,
                                      String tipo, BigDecimal valor, String descricao) {
        ContaFinanceira conta = buscar(tenantId, contaId);
        String tipoNormalizado = normalizarTipoMovimento(tipo);
        String descricaoNormalizada = normalizarObrigatorio(descricao, "Descricao");
        conta.movimentar(tipoNormalizado, valor);
        repository.save(conta);
        movimentoRepository.save(new ContaFinanceiraMovimento(
                tenantId, conta.getFilialId(), conta.getId(), tipoNormalizado, valor, descricaoNormalizada, usuarioId));
        auditoria.registrar(tenantId, usuarioId, null, conta.getFilialId(),
                "MOVIMENTAR", "CONTA_FINANCEIRA", conta.getId(),
                "tipo=" + tipoNormalizado + ";valor=" + valor + ";saldo=" + conta.getSaldo());
        return conta;
    }

    @Transactional
    public ContaFinanceira desativar(UUID tenantId, UUID usuarioId, UUID contaId) {
        ContaFinanceira conta = buscar(tenantId, contaId);
        conta.desativar();
        repository.save(conta);
        auditoria.registrar(tenantId, usuarioId, null, conta.getFilialId(),
                "DESATIVAR", "CONTA_FINANCEIRA", conta.getId(), null);
        return conta;
    }

    private String normalizarTipoConta(String tipo) {
        String valor = normalizarObrigatorio(tipo, "Tipo").toUpperCase(Locale.ROOT);
        if (!("CAIXA".equals(valor) || "BANCO".equals(valor))) {
            throw new IllegalArgumentException("Tipo de conta deve ser CAIXA ou BANCO");
        }
        return valor;
    }

    private String normalizarTipoMovimento(String tipo) {
        String valor = normalizarObrigatorio(tipo, "Tipo do movimento").toUpperCase(Locale.ROOT);
        if (!("ENTRADA".equals(valor) || "SAIDA".equals(valor))) {
            throw new IllegalArgumentException("Tipo de movimento deve ser ENTRADA ou SAIDA");
        }
        return valor;
    }

    private String normalizarObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " e obrigatorio");
        return valor.trim();
    }
}
