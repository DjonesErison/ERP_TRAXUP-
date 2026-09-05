package com.traxup.tplug.erp.venda;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoVendaRepository extends JpaRepository<PedidoVenda, UUID> {
    List<PedidoVenda> findAllByTenantIdOrderByCriadoEmDesc(UUID tenantId);
    Optional<PedidoVenda> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndNumeroIgnoreCase(UUID tenantId, String numero);
}
