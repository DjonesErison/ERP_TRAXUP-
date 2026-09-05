package com.traxup.tplug.erp.produto.api;

import com.traxup.tplug.erp.produto.Produto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProdutoResponse(
        UUID id,
        UUID tenantId,
        String codigo,
        String descricao,
        String grupo,
        String ncm,
        BigDecimal vendaPrc,
        BigDecimal compraPrc,
        String codigoBarra,
        String unidade,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm) {

    public static ProdutoResponse from(UUID tenantId, Produto produto) {
        return new ProdutoResponse(
                produto.getId(), tenantId, produto.getCodigo(), produto.getDescricao(), produto.getGrupo(), produto.getNcm(),
                produto.getVendaPrc(), produto.getCompraPrc(), produto.getCodigoBarra(), produto.getUnidade(),
                produto.isAtivo(), produto.getCriadoEm(), produto.getAtualizadoEm());
    }
}
