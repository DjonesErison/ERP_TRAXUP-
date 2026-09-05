package com.traxup.tplug.erp.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ContaFinanceiraMovimentoRepository extends JpaRepository<ContaFinanceiraMovimento, UUID> {
    List<ContaFinanceiraMovimento> findAllByTenantIdAndContaFinanceiraIdOrderByOcorridoEmDesc(
            UUID tenantId, UUID contaFinanceiraId);
}
