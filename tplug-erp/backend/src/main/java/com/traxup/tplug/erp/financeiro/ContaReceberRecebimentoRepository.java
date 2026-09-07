package com.traxup.tplug.erp.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ContaReceberRecebimentoRepository extends JpaRepository<ContaReceberRecebimento, UUID> {
    @Query("""
            SELECT recebimento FROM ContaReceberRecebimento recebimento
            WHERE recebimento.tenantId = :tenantId
              AND recebimento.contaReceberId = :contaReceberId
            ORDER BY recebimento.recebidoEm DESC, recebimento.id ASC
            """)
    List<ContaReceberRecebimento> findAllByTenantIdAndContaReceberIdOrderByRecebidoEmDesc(
            @Param("tenantId") UUID tenantId,
            @Param("contaReceberId") UUID contaReceberId);
}
