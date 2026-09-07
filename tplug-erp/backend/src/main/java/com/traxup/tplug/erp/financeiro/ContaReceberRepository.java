package com.traxup.tplug.erp.financeiro;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaReceberRepository extends JpaRepository<ContaReceber, UUID> {
    List<ContaReceber> findAllByTenantIdOrderByVencimentoAscCriadoEmDesc(UUID tenantId);
    Optional<ContaReceber> findByIdAndTenantId(UUID id, UUID tenantId);
    List<ContaReceber> findAllByTenantIdAndOrigemTipoAndOrigemIdOrderByVencimentoAscCriadoEmDesc(
            UUID tenantId, String origemTipo, UUID origemId);

    @Query("""
            SELECT conta FROM ContaReceber conta
            WHERE conta.tenantId = :tenantId
              AND (:status IS NULL OR conta.status = :status)
              AND (:vencimentoInicio IS NULL OR conta.vencimento >= :vencimentoInicio)
              AND (:vencimentoFim IS NULL OR conta.vencimento <= :vencimentoFim)
            ORDER BY conta.vencimento ASC, conta.criadoEm DESC
            """)
    List<ContaReceber> filtrar(@Param("tenantId") UUID tenantId,
                               @Param("status") String status,
                               @Param("vencimentoInicio") LocalDate vencimentoInicio,
                               @Param("vencimentoFim") LocalDate vencimentoFim);

    @Query("""
            SELECT conta FROM ContaReceber conta
            WHERE conta.tenantId = :tenantId
              AND conta.filialId = :filialId
              AND (:status IS NULL OR conta.status = :status)
              AND (:vencimentoInicio IS NULL OR conta.vencimento >= :vencimentoInicio)
              AND (:vencimentoFim IS NULL OR conta.vencimento <= :vencimentoFim)
            ORDER BY conta.vencimento ASC, conta.criadoEm DESC
            """)
    List<ContaReceber> filtrarPorFilial(@Param("tenantId") UUID tenantId,
                                        @Param("filialId") UUID filialId,
                                        @Param("status") String status,
                                        @Param("vencimentoInicio") LocalDate vencimentoInicio,
                                        @Param("vencimentoFim") LocalDate vencimentoFim);

    @Query("""
            SELECT conta FROM ContaReceber conta
            WHERE conta.tenantId = :tenantId
              AND conta.clienteId = :clienteId
              AND (:filialId IS NULL OR conta.filialId = :filialId)
              AND (:status IS NULL OR conta.status = :status)
              AND (:vencimentoInicio IS NULL OR conta.vencimento >= :vencimentoInicio)
              AND (:vencimentoFim IS NULL OR conta.vencimento <= :vencimentoFim)
            ORDER BY conta.vencimento ASC, conta.criadoEm DESC
            """)
    List<ContaReceber> filtrarPorCliente(@Param("tenantId") UUID tenantId,
                                         @Param("clienteId") UUID clienteId,
                                         @Param("filialId") UUID filialId,
                                         @Param("status") String status,
                                         @Param("vencimentoInicio") LocalDate vencimentoInicio,
                                         @Param("vencimentoFim") LocalDate vencimentoFim);

    @Query("""
            SELECT COUNT(conta) AS quantidade,
                   COALESCE(SUM(conta.valorOriginal), 0) AS valorOriginalTotal,
                   COALESCE(SUM(conta.valorRecebido), 0) AS valorLiquidadoTotal,
                   COALESCE(SUM(CASE WHEN conta.status IN ('ABERTO', 'PARCIAL')
                       THEN conta.valorOriginal - conta.valorRecebido ELSE 0 END), 0) AS saldoAtivoTotal,
                   SUM(CASE WHEN conta.status = 'ABERTO' THEN 1 ELSE 0 END) AS abertos,
                   SUM(CASE WHEN conta.status = 'PARCIAL' THEN 1 ELSE 0 END) AS parciais,
                   SUM(CASE WHEN conta.status = 'RECEBIDO' THEN 1 ELSE 0 END) AS liquidados,
                   SUM(CASE WHEN conta.status = 'CANCELADO' THEN 1 ELSE 0 END) AS cancelados
            FROM ContaReceber conta
            WHERE conta.tenantId = :tenantId
              AND (:filialId IS NULL OR conta.filialId = :filialId)
              AND (:clienteId IS NULL OR conta.clienteId = :clienteId)
              AND (:status IS NULL OR conta.status = :status)
              AND (:vencimentoInicio IS NULL OR conta.vencimento >= :vencimentoInicio)
              AND (:vencimentoFim IS NULL OR conta.vencimento <= :vencimentoFim)
            """)
    TituloFinanceiroResumoProjection resumir(@Param("tenantId") UUID tenantId,
                                             @Param("filialId") UUID filialId,
                                             @Param("clienteId") UUID clienteId,
                                             @Param("status") String status,
                                             @Param("vencimentoInicio") LocalDate vencimentoInicio,
                                             @Param("vencimentoFim") LocalDate vencimentoFim);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT conta FROM ContaReceber conta WHERE conta.id = :id AND conta.tenantId = :tenantId")
    Optional<ContaReceber> findByIdAndTenantIdForUpdate(@Param("id") UUID id,
                                                        @Param("tenantId") UUID tenantId);
}
