package com.traxup.tplug.erp.financeiro.integracao;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface IntegracaoFinanceiraTentativaRepository extends JpaRepository<IntegracaoFinanceiraTentativa, UUID> {
    List<IntegracaoFinanceiraTentativa> findTop50ByTenantIdAndIntegracaoIdOrderByIniciadoEmDesc(UUID tenantId, UUID integracaoId);
}
