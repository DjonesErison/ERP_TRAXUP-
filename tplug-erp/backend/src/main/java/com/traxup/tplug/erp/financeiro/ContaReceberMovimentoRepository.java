package com.traxup.tplug.erp.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContaReceberMovimentoRepository extends JpaRepository<ContaReceberMovimento, UUID> {
    List<ContaReceberMovimento> findAllByTenantIdAndContaReceberIdOrderByDataMovimentoDescCriadoEmDesc(
            UUID tenantId, UUID contaReceberId);
}
