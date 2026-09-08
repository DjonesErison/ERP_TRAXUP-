package com.traxup.tplug.erp.crm.api;

import com.traxup.tplug.erp.crm.ClienteRfvProjection;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record ClienteRfvResponse(
        UUID clienteId,
        String nomeRazaoSocial,
        String nomeFantasia,
        String email,
        String telefone,
        Instant ultimaCompraEm,
        long diasDesdeUltimaCompra,
        long quantidadeCompras,
        BigDecimal valorTotalCompras,
        BigDecimal ticketMedio
) {
    public static ClienteRfvResponse from(ClienteRfvProjection p) {
        BigDecimal total = p.getValorTotalCompras() == null ? BigDecimal.ZERO : p.getValorTotalCompras();
        long compras = p.getQuantidadeCompras();
        BigDecimal ticket = compras == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(compras), 2, java.math.RoundingMode.HALF_UP);
        long dias = p.getUltimaCompraEm() == null ? 0 : Math.max(0, ChronoUnit.DAYS.between(p.getUltimaCompraEm(), Instant.now()));
        return new ClienteRfvResponse(p.getClienteId(), p.getNomeRazaoSocial(), p.getNomeFantasia(), p.getEmail(), p.getTelefone(),
                p.getUltimaCompraEm(), dias, compras, total, ticket);
    }
}
