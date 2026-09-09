package com.traxup.tplug.erp.fiscal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FiscalSolicitacaoRepository extends JpaRepository<FiscalSolicitacao, UUID> {
    Optional<FiscalSolicitacao> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<FiscalSolicitacao> findByTenantIdAndPedidoVendaIdAndModeloAndAmbiente(
            UUID tenantId, UUID pedidoVendaId, String modelo, String ambiente);
    List<FiscalSolicitacao> findAllByTenantIdOrderByCriadoEmDesc(UUID tenantId);
}
