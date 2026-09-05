package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarRecebimentoRequest(
        @NotNull @DecimalMin(value = "0.0001") BigDecimal valor,
        @NotNull @PastOrPresent LocalDate dataRecebimento,
        @Size(max = 500) String observacao) {
}
