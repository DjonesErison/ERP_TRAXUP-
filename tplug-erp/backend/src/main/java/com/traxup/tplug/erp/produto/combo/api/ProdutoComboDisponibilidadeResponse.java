package com.traxup.tplug.erp.produto.combo.api;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoComboDisponibilidadeResponse(
        UUID produtoId,
        UUID filialId,
        BigDecimal quantidadeDisponivel
) {}
