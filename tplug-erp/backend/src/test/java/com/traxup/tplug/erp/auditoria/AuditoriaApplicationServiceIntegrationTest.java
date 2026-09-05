package com.traxup.tplug.erp.auditoria;

import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AuditoriaApplicationServiceIntegrationTest {

    @Autowired
    private AuditoriaApplicationService auditoriaApplicationService;

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    void deveRegistrarEIsolarAuditoriaPorTenant() {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant Auditoria A"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant Auditoria B"));
        UUID usuarioId = UUID.randomUUID();
        UUID entidadeId = UUID.randomUUID();

        AuditoriaEvento evento = auditoriaApplicationService.registrar(
                tenantA.getId(),
                usuarioId,
                null,
                null,
                "criar",
                "empresa",
                entidadeId,
                "razaoSocial alterada");

        List<AuditoriaEvento> eventosTenantA = auditoriaApplicationService.listarPorTenant(tenantA.getId());
        List<AuditoriaEvento> eventosTenantB = auditoriaApplicationService.listarPorTenant(tenantB.getId());

        assertEquals(1, eventosTenantA.size());
        assertEquals(evento.id(), eventosTenantA.getFirst().id());
        assertEquals(tenantA.getId(), eventosTenantA.getFirst().tenantId());
        assertEquals(usuarioId, eventosTenantA.getFirst().usuarioId());
        assertEquals("CRIAR", eventosTenantA.getFirst().operacao());
        assertEquals("EMPRESA", eventosTenantA.getFirst().entidade());
        assertEquals(entidadeId, eventosTenantA.getFirst().entidadeId());
        assertTrue(eventosTenantB.isEmpty());
    }
}
