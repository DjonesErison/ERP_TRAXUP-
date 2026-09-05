package com.traxup.tplug.erp.pessoa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PessoaRepository extends JpaRepository<Pessoa, UUID> {
    List<Pessoa> findAllByTenantIdOrderByNomeRazaoSocialAsc(UUID tenantId);
    List<Pessoa> findAllByTenantIdAndClienteTrueOrderByNomeRazaoSocialAsc(UUID tenantId);
    List<Pessoa> findAllByTenantIdAndFornecedorTrueOrderByNomeRazaoSocialAsc(UUID tenantId);
    Optional<Pessoa> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndCpfCnpj(UUID tenantId, String cpfCnpj);
}
