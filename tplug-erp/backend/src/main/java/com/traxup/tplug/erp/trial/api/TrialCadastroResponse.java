package com.traxup.tplug.erp.trial.api;

import java.time.Instant;
import java.util.UUID;

public record TrialCadastroResponse(
        UUID trialId,
        UUID tenantId,
        Instant expiraEm,
        String status,
        String proximoPasso,
        String ativacaoToken
) {}
