package com.traxup.tplug.erp.produto.combo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProdutoComboComponenteRepository extends JpaRepository<ProdutoComboComponente, UUID> {
    List<ProdutoComboComponente> findAllByTenantIdAndComboProdutoIdOrderByComponenteProdutoIdAsc(UUID tenantId, UUID comboProdutoId);
    boolean existsByTenantIdAndComboProdutoId(UUID tenantId, UUID comboProdutoId);
    boolean existsByTenantIdAndComponenteProdutoId(UUID tenantId, UUID componenteProdutoId);
    void deleteAllByTenantIdAndComboProdutoId(UUID tenantId, UUID comboProdutoId);
}
