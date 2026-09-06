package com.traxup.tplug.erp.financeiro.pagamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CondicaoPagamentoParcelaRepository extends JpaRepository<CondicaoPagamentoParcela, UUID> {
    List<CondicaoPagamentoParcela> findAllByTenantIdAndCondicaoPagamentoIdOrderByNumeroAsc(UUID tenantId, UUID condicaoPagamentoId);
    void deleteAllByTenantIdAndCondicaoPagamentoId(UUID tenantId, UUID condicaoPagamentoId);
}
