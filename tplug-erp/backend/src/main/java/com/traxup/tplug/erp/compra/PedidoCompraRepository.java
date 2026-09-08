package com.traxup.tplug.erp.compra;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, UUID> {
    List<PedidoCompra> findAllByTenantIdOrderByCriadoEmDesc(UUID tenantId);
    Optional<PedidoCompra> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndNumeroIgnoreCase(UUID tenantId, String numero);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pedido FROM PedidoCompra pedido WHERE pedido.id = :id AND pedido.tenantId = :tenantId")
    Optional<PedidoCompra> findByIdAndTenantIdForUpdate(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}
