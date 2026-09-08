package com.traxup.tplug.erp.produto.combo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProdutoComboGrupoRepository extends JpaRepository<ProdutoComboGrupo, UUID> {
    List<ProdutoComboGrupo> findAllByTenantIdAndComboProdutoIdOrderByNomeAsc(UUID tenantId, UUID comboProdutoId);
    Optional<ProdutoComboGrupo> findByIdAndTenantIdAndComboProdutoId(UUID id, UUID tenantId, UUID comboProdutoId);
    boolean existsByTenantIdAndComboProdutoIdAndNomeIgnoreCase(UUID tenantId, UUID comboProdutoId, String nome);
}
