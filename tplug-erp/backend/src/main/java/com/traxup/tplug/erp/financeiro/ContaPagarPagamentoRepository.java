package com.traxup.tplug.erp.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ContaPagarPagamentoRepository extends JpaRepository<ContaPagarPagamento, UUID> {
    @Query("""
            SELECT pagamento FROM ContaPagarPagamento pagamento
            WHERE pagamento.tenantId = :tenantId
              AND pagamento.contaPagarId = :contaPagarId
            ORDER BY pagamento.pagoEm DESC, pagamento.id ASC
            """)
    List<ContaPagarPagamento> findAllByTenantIdAndContaPagarIdOrderByPagoEmDesc(
            @Param("tenantId") UUID tenantId,
            @Param("contaPagarId") UUID contaPagarId);
}
