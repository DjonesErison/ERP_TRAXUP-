package com.traxup.tplug.erp.financeiro.pagamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormaPagamentoRepository extends JpaRepository<FormaPagamento, UUID> {
    List<FormaPagamento> findAllByTenantIdOrderByNomeAsc(UUID tenantId);
    Optional<FormaPagamento> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndCodigoIgnoreCase(UUID tenantId, String codigo);
}
