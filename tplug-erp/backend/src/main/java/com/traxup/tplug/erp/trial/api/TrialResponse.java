package com.traxup.tplug.erp.trial.api;

import java.time.Instant;
import java.util.UUID;

public record TrialResponse(UUID trialId, UUID tenantId, UUID empresaId, UUID filialId,
                            String email, Instant expiraEm, String proximoPasso) {}
