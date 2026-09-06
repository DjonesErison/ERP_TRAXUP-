package com.traxup.tplug.erp.financeiro.integracao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface IntegracaoFinanceiraTentativaRepository extends JpaRepository<IntegracaoFinanceiraTentativa, UUID> {
    List<IntegracaoFinanceiraTentativa> findTop50ByTenantIdAndIntegracaoIdOrderByIniciadoEmDesc(UUID tenantId, UUID integracaoId);

    @Query("""
            select t from IntegracaoFinanceiraTentativa t
            where t.tenantId = :tenantId
              and t.integracaoId = :integracaoId
              and (:status is null or t.status = :status)
              and (:inicio is null or t.iniciadoEm >= :inicio)
              and (:fim is null or t.iniciadoEm <= :fim)
            order by t.iniciadoEm desc
            """)
    List<IntegracaoFinanceiraTentativa> filtrar(
            @Param("tenantId") UUID tenantId,
            @Param("integracaoId") UUID integracaoId,
            @Param("status") String status,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim,
            Pageable pageable);
}
