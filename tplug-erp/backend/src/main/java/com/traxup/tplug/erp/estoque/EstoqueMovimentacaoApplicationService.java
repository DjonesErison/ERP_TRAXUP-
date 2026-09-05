package com.traxup.tplug.erp.estoque;

import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class EstoqueMovimentacaoApplicationService {

    private static final Set<String> TIPOS_ITEM = Set.of("PRODUTO", "GRADE");
    private static final Set<String> TIPOS_MOVIMENTO = Set.of("ENTRADA", "SAIDA", "AJUSTE");

    private final EstoqueSaldoRepository saldoRepository;
    private final EstoqueMovimentacaoRepository movimentacaoRepository;
    private final FilialRepository filialRepository;

    public EstoqueMovimentacaoApplicationService(EstoqueSaldoRepository saldoRepository,
                                                  EstoqueMovimentacaoRepository movimentacaoRepository,
                                                  FilialRepository filialRepository) {
        this.saldoRepository = saldoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.filialRepository = filialRepository;
    }

    @Transactional
    public EstoqueMovimentacao movimentar(UUID tenantId, UUID filialId, String tipoItem, UUID itemId,
                                           String tipoMovimento, BigDecimal quantidade, String motivo,
                                           UUID usuarioId) {
        validarFilial(tenantId, filialId);
        String item = tipoItem.toUpperCase();
        String movimento = tipoMovimento.toUpperCase();
        if (!TIPOS_ITEM.contains(item)) throw new IllegalArgumentException("Tipo de item invalido");
        if (!TIPOS_MOVIMENTO.contains(movimento)) throw new IllegalArgumentException("Tipo de movimento invalido");
        if (quantidade == null || quantidade.signum() <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero");

        EstoqueSaldo saldo = saldoRepository.findByTenantIdAndFilialIdAndTipoItemAndItemId(tenantId, filialId, item, itemId)
                .orElseGet(() -> new EstoqueSaldo(tenantId, filialId, item, itemId));
        BigDecimal anterior = saldo.getQuantidade();
        BigDecimal posterior = switch (movimento) {
            case "ENTRADA" -> anterior.add(quantidade);
            case "SAIDA" -> anterior.subtract(quantidade);
            case "AJUSTE" -> quantidade;
            default -> throw new IllegalStateException("Movimento nao suportado");
        };
        if (posterior.signum() < 0) throw new IllegalArgumentException("Saldo insuficiente para saida");

        saldo.definirQuantidade(posterior);
        saldoRepository.save(saldo);
        return movimentacaoRepository.save(new EstoqueMovimentacao(
                tenantId, filialId, item, itemId, movimento, quantidade, anterior, posterior, motivo, usuarioId));
    }

    @Transactional(readOnly = true)
    public List<EstoqueMovimentacao> listar(UUID tenantId, UUID filialId) {
        validarFilial(tenantId, filialId);
        return movimentacaoRepository.findAllByTenantIdAndFilialIdOrderByCriadoEmDesc(tenantId, filialId);
    }

    private void validarFilial(UUID tenantId, UUID filialId) {
        if (!filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }
    }
}
