package com.traxup.tplug.erp.produto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    List<Produto> findAllByTenantIdOrderByDescricaoAsc(UUID tenantId);

    Optional<Produto> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Produto> findFirstByTenantIdAndCodigoBarraAndAtivoTrue(UUID tenantId, String codigoBarra);

    boolean existsByTenantIdAndCodigoIgnoreCase(UUID tenantId, String codigo);
}
