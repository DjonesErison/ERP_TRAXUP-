package com.traxup.tplug.erp.produto.grade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GradeProdutoRepository extends JpaRepository<GradeProduto, UUID> {

    List<GradeProduto> findAllByTenantIdAndProdutoIdOrderByDescricaoGradeAsc(UUID tenantId, UUID produtoId);

    Optional<GradeProduto> findByIdAndTenantId(UUID id, UUID tenantId);

    List<GradeProduto> findAllByTenantIdAndCodigoBarraAndAtivoTrue(UUID tenantId, String codigoBarra);

    boolean existsByTenantIdAndCodigoGradeIgnoreCase(UUID tenantId, String codigoGrade);
}
