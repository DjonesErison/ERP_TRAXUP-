package com.traxup.tplug.erp.trial.api;

import jakarta.validation.constraints.*;

public record CriarTrialRequest(
        @NotBlank @Size(max=200) String empresa,
        @Size(max=14) String documento,
        @NotBlank @Size(max=150) String responsavel,
        @NotBlank @Email @Size(max=254) String email,
        @NotBlank @Size(max=30) String telefone,
        @Size(max=100) String segmento,
        @Min(1) @Max(999) Integer quantidadeLojas,
        @NotBlank @Size(min=12,max=72) String senha,
        @AssertTrue(message="O aceite dos termos e da politica de privacidade e obrigatorio") boolean aceiteTermos) {}
