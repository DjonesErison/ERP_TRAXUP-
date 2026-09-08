package com.traxup.tplug.erp.produto.combo.api;

import com.traxup.tplug.erp.produto.combo.ProdutoComboGrupo;
import com.traxup.tplug.erp.produto.combo.ProdutoComboGrupoOpcao;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoComboGrupoResponse(
        UUID id,
        UUID produtoId,
        String nome,
        int minimoEscolhas,
        int maximoEscolhas
) {
    public static ProdutoComboGrupoResponse from(ProdutoComboGrupo grupo) {
        return new ProdutoComboGrupoResponse(grupo.getId(), grupo.getComboProdutoId(), grupo.getNome(),
                grupo.getMinimoEscolhas(), grupo.getMaximoEscolhas());
    }

    public record Opcao(UUID id, UUID produtoId, BigDecimal quantidade, BigDecimal valorAdicional) {
        public static Opcao from(ProdutoComboGrupoOpcao opcao) {
            return new Opcao(opcao.getId(), opcao.getProdutoId(), opcao.getQuantidade(), opcao.getValorAdicional());
        }
    }
}
