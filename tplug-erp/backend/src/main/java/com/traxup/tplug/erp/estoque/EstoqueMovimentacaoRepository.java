package com.traxup.tplug.erp.estoque;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EstoqueMovimentacaoRepository extends JpaRepository<EstoqueMovimentacao, UUID> {
    List<EstoqueMovimentacao> findAllByTenantIdAndFilialIdOrderByCriadoEmDesc(UUID tenantId, UUID filialId);
}
