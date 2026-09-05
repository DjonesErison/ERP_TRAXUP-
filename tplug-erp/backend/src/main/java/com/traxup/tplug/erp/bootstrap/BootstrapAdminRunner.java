package com.traxup.tplug.erp.bootstrap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bootstrap.admin.enabled", havingValue = "true")
public class BootstrapAdminRunner implements ApplicationRunner {

    private final BootstrapAdminService bootstrapAdminService;
    private final String tenantNome;
    private final String adminNome;
    private final String adminEmail;
    private final String adminSenha;

    public BootstrapAdminRunner(
            BootstrapAdminService bootstrapAdminService,
            @Value("${bootstrap.admin.tenant-name:}") String tenantNome,
            @Value("${bootstrap.admin.name:}") String adminNome,
            @Value("${bootstrap.admin.email:}") String adminEmail,
            @Value("${bootstrap.admin.password:}") String adminSenha) {
        this.bootstrapAdminService = bootstrapAdminService;
        this.tenantNome = tenantNome;
        this.adminNome = adminNome;
        this.adminEmail = adminEmail;
        this.adminSenha = adminSenha;
    }

    @Override
    public void run(ApplicationArguments args) {
        bootstrapAdminService.executar(tenantNome, adminNome, adminEmail, adminSenha);
    }
}
