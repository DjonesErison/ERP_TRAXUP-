package com.traxup.tplug.erp.pessoa.contato;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PessoaContatoRepository extends JpaRepository<PessoaContato, UUID> {
    List<PessoaContato> findAllByTenantIdAndPessoaIdOrderByPrincipalDescNomeAsc(UUID tenantId, UUID pessoaId);
}
