package com.traxup.tplug.erp.trial.api;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record TrialReenviarRequest(@NotNull UUID tenantId,@NotBlank @Email @Size(max=254) String email) {}
