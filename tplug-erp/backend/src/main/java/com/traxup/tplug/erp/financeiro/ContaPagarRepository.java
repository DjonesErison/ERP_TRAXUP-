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

public interface ContaPagarRepository extends JpaRepository<ContaPagar, UUID> {
    List<ContaPagar> findAllByTenantIdOrderByVencimentoAscCriadoEmDesc(UUID tenantId);
    Optional<ContaPagar> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("""
            SELECT conta FROM ContaPagar conta
            WHERE conta.tenantId = :tenantId
              AND (:status IS NULL OR conta.status = :status)
              AND conta.vencimento >= COALESCE(:vencimentoInicio, conta.vencimento)
              AND conta.vencimento <= COALESCE(:vencimentoFim, conta.vencimento)
            ORDER BY conta.vencimento ASC, conta.criadoEm DESC
            """)
    List<ContaPagar> filtrar(@Param("tenantId") UUID tenantId,
                             @Param("status") String status,
                             @Param("vencimentoInicio") LocalDate vencimentoInicio,
                             @Param("vencimentoFim") LocalDate vencimentoFim);

    @Query("""
            SELECT conta FROM ContaPagar conta
            WHERE conta.tenantId = :tenantId
              AND conta.filialId = :filialId
              AND (:status IS NULL OR conta.status = :status)
              AND conta.vencimento >= COALESCE(:vencimentoInicio, conta.vencimento)
              AND conta.vencimento <= COALESCE(:vencimentoFim, conta.vencimento)
            ORDER BY conta.vencimento ASC, conta.criadoEm DESC
            """)
    List<ContaPagar> filtrarPorFilial(@Param("tenantId") UUID tenantId,
                                      @Param("filialId") UUID filialId,
                                      @Param("status") String status,
                                      @Param("vencimentoInicio") LocalDate vencimentoInicio,
                                      @Param("vencimentoFim") LocalDate vencimentoFim);

    @Query("""
            SELECT conta FROM ContaPagar conta
            WHERE conta.tenantId = :tenantId
              AND conta.fornecedorId = :fornecedorId
              AND (:filialId IS NULL OR conta.filialId = :filialId)
              AND (:status IS NULL OR conta.status = :status)
              AND conta.vencimento >= COALESCE(:vencimentoInicio, conta.vencimento)
              AND conta.vencimento <= COALESCE(:vencimentoFim, conta.vencimento)
            ORDER BY conta.vencimento ASC, conta.criadoEm DESC
            """)
    List<ContaPagar> filtrarPorFornecedor(@Param("tenantId") UUID tenantId,
                                          @Param("fornecedorId") UUID fornecedorId,
                                          @Param("filialId") UUID filialId,
                                          @Param("status") String status,
                                          @Param("vencimentoInicio") LocalDate vencimentoInicio,
                                          @Param("vencimentoFim") LocalDate vencimentoFim);

    @Query("""
            SELECT COUNT(conta) AS quantidade,
                   COALESCE(SUM(conta.valorOriginal), 0) AS valorOriginalTotal,
                   COALESCE(SUM(conta.valorPago), 0) AS valorLiquidadoTotal,
                   COALESCE(SUM(CASE WHEN conta.status IN ('ABERTO', 'PARCIAL')
                       THEN conta.valorOriginal - conta.valorPago ELSE 0 END), 0) AS saldoAtivoTotal,
                   SUM(CASE WHEN conta.status = 'ABERTO' THEN 1 ELSE 0 END) AS abertos,
                   SUM(CASE WHEN conta.status = 'PARCIAL' THEN 1 ELSE 0 END) AS parciais,
                   SUM(CASE WHEN conta.status = 'PAGO' THEN 1 ELSE 0 END) AS liquidados,
                   SUM(CASE WHEN conta.status = 'CANCELADO' THEN 1 ELSE 0 END) AS cancelados
            FROM ContaPagar conta
            WHERE conta.tenantId = :tenantId
              AND (:filialId IS NULL OR conta.filialId = :filialId)
              AND (:fornecedorId IS NULL OR conta.fornecedorId = :fornecedorId)
              AND (:status IS NULL OR conta.status = :status)
              AND conta.vencimento >= COALESCE(:vencimentoInicio, conta.vencimento)
              AND conta.vencimento <= COALESCE(:vencimentoFim, conta.vencimento)
            """)
    TituloFinanceiroResumoProjection resumir(@Param("tenantId") UUID tenantId,
                                             @Param("filialId") UUID filialId,
                                             @Param("fornecedorId") UUID fornecedorId,
                                             @Param("status") String status,
                                             @Param("vencimentoInicio") LocalDate vencimentoInicio,
                                             @Param("vencimentoFim") LocalDate vencimentoFim);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT conta FROM ContaPagar conta WHERE conta.id = :id AND conta.tenantId = :tenantId")
    Optional<ContaPagar> findByIdAndTenantIdForUpdate(@Param("id") UUID id,
                                                      @Param("tenantId") UUID tenantId);
}
