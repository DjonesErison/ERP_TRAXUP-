package com.traxup.tplug.erp.pessoa.endereco;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PessoaEnderecoRepository extends JpaRepository<PessoaEndereco, UUID> {
    List<PessoaEndereco> findAllByTenantIdAndPessoaIdOrderByPrincipalDescCriadoEmAsc(UUID tenantId, UUID pessoaId);
    Optional<PessoaEndereco> findByIdAndTenantIdAndPessoaId(UUID id, UUID tenantId, UUID pessoaId);
}
