package com.traxup.tplug.erp.trial.api;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;

public record TrialReenviarRequest(
        @JsonAlias("codigo_empresa") @Pattern(regexp = "[0-9]{4}") String codigoEmpresa,
        UUID tenantId,
        @NotBlank @Email @Size(max = 254) String email) {
    @AssertTrue(message = "Informe o codigo da empresa de 4 digitos")
    @JsonIgnore
    public boolean isIdentificacaoValida() { return (codigoEmpresa != null) != (tenantId != null); }
}
