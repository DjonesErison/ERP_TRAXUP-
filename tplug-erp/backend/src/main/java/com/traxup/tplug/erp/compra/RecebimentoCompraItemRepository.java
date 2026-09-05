package com.traxup.tplug.erp.compra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecebimentoCompraItemRepository extends JpaRepository<RecebimentoCompraItem, UUID> {
    List<RecebimentoCompraItem> findAllByTenantIdAndRecebimentoIdOrderByCriadoEmAsc(UUID tenantId, UUID recebimentoId);
}
