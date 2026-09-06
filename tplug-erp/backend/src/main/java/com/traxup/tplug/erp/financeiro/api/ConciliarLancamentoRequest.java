package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConciliarLancamentoRequest(@NotNull UUID movimentoId) {}
