package com.traxup.tplug.erp.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContaPagarPagamentoRepository extends JpaRepository<ContaPagarPagamento, UUID> {
    List<ContaPagarPagamento> findAllByTenantIdAndContaPagarIdOrderByPagoEmDescIdAsc(UUID tenantId, UUID contaPagarId);
}
