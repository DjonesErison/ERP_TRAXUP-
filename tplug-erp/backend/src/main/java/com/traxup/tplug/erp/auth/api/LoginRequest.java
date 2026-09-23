package com.traxup.tplug.erp.auth.api;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;

public record LoginRequest(
        @JsonAlias("codigo_empresa") @Pattern(regexp = "[0-9]{4}") String codigoEmpresa,
        UUID tenantId,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 72) String senha) {
    @AssertTrue(message = "Informe o codigo da empresa de 4 digitos")
    @JsonIgnore
    public boolean isIdentificacaoValida() { return (codigoEmpresa != null) != (tenantId != null); }
}
