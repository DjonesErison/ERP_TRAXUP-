package com.traxup.tplug.erp.produto.combo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProdutoComboGrupoOpcaoRepository extends JpaRepository<ProdutoComboGrupoOpcao, UUID> {
    List<ProdutoComboGrupoOpcao> findAllByTenantIdAndGrupoIdOrderByProdutoIdAsc(UUID tenantId, UUID grupoId);
    Optional<ProdutoComboGrupoOpcao> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndGrupoIdAndProdutoId(UUID tenantId, UUID grupoId, UUID produtoId);
}
