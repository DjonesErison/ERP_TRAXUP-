package com.traxup.tplug.erp.estoque;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstoqueSaldoRepository extends JpaRepository<EstoqueSaldo, UUID> {

    List<EstoqueSaldo> findAllByTenantIdAndFilialIdOrderByTipoItemAscItemIdAsc(UUID tenantId, UUID filialId);

    Optional<EstoqueSaldo> findByTenantIdAndFilialIdAndTipoItemAndItemId(
            UUID tenantId, UUID filialId, String tipoItem, UUID itemId);
}
