package com.traxup.tplug.erp.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContaReceberRecebimentoRepository extends JpaRepository<ContaReceberRecebimento, UUID> {
    List<ContaReceberRecebimento> findAllByTenantIdAndContaReceberIdOrderByRecebidoEmDesc(
            UUID tenantId, UUID contaReceberId);
}
