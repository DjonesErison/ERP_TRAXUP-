package com.traxup.tplug.erp.crm;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteInteracaoRepository extends JpaRepository<ClienteInteracao, UUID> {
    Optional<ClienteInteracao> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("""
            select i from ClienteInteracao i
            where i.tenantId = :tenantId
              and (:filialId is null or i.filialId = :filialId)
              and (:clienteId is null or i.clienteId = :clienteId)
              and (:canal is null or i.canal = :canal)
              and (:resultado is null or i.resultado = :resultado)
              and (cast(:inicio as instant) is null or i.ocorridoEm >= :inicio)
              and (cast(:fim as instant) is null or i.ocorridoEm <= :fim)
            order by i.ocorridoEm desc, i.id asc
            """)
    List<ClienteInteracao> buscar(@Param("tenantId") UUID tenantId,
                                  @Param("filialId") UUID filialId,
                                  @Param("clienteId") UUID clienteId,
                                  @Param("canal") String canal,
                                  @Param("resultado") String resultado,
                                  @Param("inicio") Instant inicio,
                                  @Param("fim") Instant fim,
                                  Pageable pageable);
}
