package com.traxup.tplug.erp.compra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecebimentoCompraRepository extends JpaRepository<RecebimentoCompra, UUID> {
    List<RecebimentoCompra> findAllByTenantIdOrderByRecebidoEmDesc(UUID tenantId);
    Optional<RecebimentoCompra> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndPedidoCompraId(UUID tenantId, UUID pedidoCompraId);
}
