package com.traxup.tplug.erp.contabilidade;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "contabilidade.sped.worker", name = "enabled",
        havingValue = "true")
public class SpedAgendamentoConfiguration {
}
