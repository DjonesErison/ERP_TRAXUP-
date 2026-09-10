package com.traxup.tplug.erp.fiscal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "fiscal.storage", name = "enabled",
        havingValue = "true")
public class FiscalArquivoAgendamentoConfiguration {
}
