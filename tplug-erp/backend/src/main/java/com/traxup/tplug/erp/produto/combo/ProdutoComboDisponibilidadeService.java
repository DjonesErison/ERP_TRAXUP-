package com.traxup.tplug.erp.produto.combo;

import com.traxup.tplug.erp.estoque.EstoqueSaldoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProdutoComboDisponibilidadeService {
    private final ProdutoComboApplicationService comboService;
    private final EstoqueSaldoRepository estoqueSaldoRepository;

    public ProdutoComboDisponibilidadeService(ProdutoComboApplicationService comboService,
                                               EstoqueSaldoRepository estoqueSaldoRepository) {
        this.comboService = comboService;
        this.estoqueSaldoRepository = estoqueSaldoRepository;
    }

    public BigDecimal calcular(UUID tenantId, UUID filialId, UUID comboProdutoId) {
        var componentes = comboService.listar(tenantId, comboProdutoId);
        if (componentes.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal disponibilidade = null;
        for (var componente : componentes) {
            BigDecimal saldo = estoqueSaldoRepository
                    .findByTenantIdAndFilialIdAndTipoItemAndItemId(
                            tenantId, filialId, "PRODUTO", componente.getComponenteProdutoId())
                    .map(s -> s.getQuantidade().max(BigDecimal.ZERO))
                    .orElse(BigDecimal.ZERO);
            BigDecimal possiveis = saldo.divide(componente.getQuantidade(), 0, RoundingMode.FLOOR);
            disponibilidade = disponibilidade == null ? possiveis : disponibilidade.min(possiveis);
        }
        return disponibilidade == null ? BigDecimal.ZERO : disponibilidade;
    }
}
