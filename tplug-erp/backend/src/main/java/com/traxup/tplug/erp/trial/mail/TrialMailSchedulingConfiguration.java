package com.traxup.tplug.erp.trial.mail;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = "trial.mail.enabled", havingValue = "true")
public class TrialMailSchedulingConfiguration {
}
