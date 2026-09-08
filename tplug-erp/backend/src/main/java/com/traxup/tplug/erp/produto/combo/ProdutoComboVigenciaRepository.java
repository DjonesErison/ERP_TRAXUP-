package com.traxup.tplug.erp.produto.combo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProdutoComboVigenciaRepository extends JpaRepository<ProdutoComboVigencia, UUID> {
    Optional<ProdutoComboVigencia> findByTenantIdAndComboProdutoId(UUID tenantId, UUID comboProdutoId);
    void deleteByTenantIdAndComboProdutoId(UUID tenantId, UUID comboProdutoId);
}
