package com.traxup.tplug.erp.estoque;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstoqueSaldoRepository extends JpaRepository<EstoqueSaldo, UUID> {

    List<EstoqueSaldo> findAllByTenantIdAndFilialIdOrderByTipoItemAscItemIdAsc(UUID tenantId, UUID filialId);

    Optional<EstoqueSaldo> findByTenantIdAndFilialIdAndTipoItemAndItemId(
            UUID tenantId, UUID filialId, String tipoItem, UUID itemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s from EstoqueSaldo s
             where s.tenantId = :tenantId
               and s.filialId = :filialId
               and s.tipoItem = :tipoItem
               and s.itemId = :itemId
            """)
    Optional<EstoqueSaldo> buscarParaAtualizar(@Param("tenantId") UUID tenantId,
                                               @Param("filialId") UUID filialId,
                                               @Param("tipoItem") String tipoItem,
                                               @Param("itemId") UUID itemId);
}
