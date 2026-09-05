package com.traxup.tplug.erp.produto.grade.api;

import com.traxup.tplug.erp.produto.grade.GradeProduto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GradeProdutoResponse(
        UUID id,
        UUID produtoId,
        String codigoGrade,
        String descricaoGrade,
        String codigoBarra,
        BigDecimal vendaPrc,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm) {

    public static GradeProdutoResponse from(GradeProduto grade) {
        return new GradeProdutoResponse(
                grade.getId(),
                grade.getProduto().getId(),
                grade.getCodigoGrade(),
                grade.getDescricaoGrade(),
                grade.getCodigoBarra(),
                grade.getVendaPrc(),
                grade.isAtivo(),
                grade.getCriadoEm(),
                grade.getAtualizadoEm());
    }
}
