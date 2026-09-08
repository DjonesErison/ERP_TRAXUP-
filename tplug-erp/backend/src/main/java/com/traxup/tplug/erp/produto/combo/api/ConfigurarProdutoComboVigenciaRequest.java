package com.traxup.tplug.erp.produto.combo.api;

import java.time.Instant;

public record ConfigurarProdutoComboVigenciaRequest(
        Instant vigenciaInicio,
        Instant vigenciaFim
) {}
