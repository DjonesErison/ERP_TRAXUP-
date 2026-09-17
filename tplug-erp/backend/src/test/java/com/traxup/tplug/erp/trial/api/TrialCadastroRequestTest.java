package com.traxup.tplug.erp.trial.api;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TrialCadastroRequestTest {
    @Test
    void rejeitaDocumentoInvalidoETermosNaoAceitos() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var request = new TrialCadastroRequest("Admin", "Loja", "Loja LTDA", "123", "87999999999",
                    "admin@loja.com", "Varejo", 1, false, "2026-09", "idem");
            assertThat(validator.validate(request)).hasSizeGreaterThanOrEqualTo(2);
        }
    }
}
