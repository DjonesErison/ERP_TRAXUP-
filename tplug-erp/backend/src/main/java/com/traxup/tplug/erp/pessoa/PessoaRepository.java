package com.traxup.tplug.erp.pessoa;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PessoaRepository extends JpaRepository<Pessoa, UUID> {
    List<Pessoa> findAllByTenantIdOrderByNomeRazaoSocialAsc(UUID tenantId);
    List<Pessoa> findAllByTenantIdAndClienteTrueOrderByNomeRazaoSocialAsc(UUID tenantId);
    List<Pessoa> findAllByTenantIdAndFornecedorTrueOrderByNomeRazaoSocialAsc(UUID tenantId);
    Optional<Pessoa> findByIdAndTenantId(UUID id, UUID tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pessoa p where p.id = :id and p.tenant.id = :tenantId")
    Optional<Pessoa> findByIdAndTenantIdForUpdate(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    boolean existsByTenantIdAndCpfCnpj(UUID tenantId, String cpfCnpj);
}
