package com.traxup.tplug.erp.trial.api;

import jakarta.validation.constraints.*;

public record TrialCadastroRequest(
        @NotBlank @Size(max = 150) String nomeCompleto,
        @NotBlank @Size(max = 200) String nomeEmpresa,
        @NotBlank @Size(max = 200) String razaoSocial,
        @NotBlank @Pattern(regexp = "\\d{11}|\\d{14}") String documento,
        @NotBlank @Size(max = 20) String telefone,
        @NotBlank @Email @Size(max = 254) String email,
        @Size(max = 80) String segmento,
        @NotNull @Min(1) Integer quantidadeLojas,
        @AssertTrue Boolean aceitouTermos,
        @NotBlank @Size(max = 40) String termosVersao,
        @NotBlank @Size(max = 100) String idempotencyKey
 ) {
    public TrialCadastroRequest {
        documento = documento == null ? null : documento.replaceAll("[.\\s/-]", "");
    }
}
