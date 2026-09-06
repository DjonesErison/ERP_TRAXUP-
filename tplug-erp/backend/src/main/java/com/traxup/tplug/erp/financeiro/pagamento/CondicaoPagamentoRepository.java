package com.traxup.tplug.erp.financeiro.pagamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CondicaoPagamentoRepository extends JpaRepository<CondicaoPagamento, UUID> {
    List<CondicaoPagamento> findAllByTenantIdOrderByNomeAsc(UUID tenantId);
    Optional<CondicaoPagamento> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndCodigoIgnoreCase(UUID tenantId, String codigo);
}
