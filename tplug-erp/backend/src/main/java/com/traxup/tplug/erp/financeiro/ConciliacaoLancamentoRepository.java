package com.traxup.tplug.erp.financeiro;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConciliacaoLancamentoRepository extends JpaRepository<ConciliacaoLancamento, UUID> {
    List<ConciliacaoLancamento> findAllByTenantIdAndContaFinanceiraIdOrderByOcorridoEmDesc(UUID tenantId, UUID contaFinanceiraId);
    Optional<ConciliacaoLancamento> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<ConciliacaoLancamento> findByTenantIdAndContaFinanceiraIdAndOrigemAndReferenciaExterna(
            UUID tenantId, UUID contaFinanceiraId, String origem, String referenciaExterna);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT lancamento FROM ConciliacaoLancamento lancamento " +
            "WHERE lancamento.id = :id AND lancamento.tenantId = :tenantId")
    Optional<ConciliacaoLancamento> findByIdAndTenantIdForUpdate(@Param("id") UUID id,
                                                                 @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT id FROM contas_financeiras " +
            "WHERE tenant_id = :tenantId AND id = :contaId FOR UPDATE", nativeQuery = true)
    Optional<UUID> bloquearContaParaImportacao(@Param("tenantId") UUID tenantId,
                                                @Param("contaId") UUID contaId);

    boolean existsByTenantIdAndMovimentoIdAndIdNot(UUID tenantId, UUID movimentoId, UUID id);
}
