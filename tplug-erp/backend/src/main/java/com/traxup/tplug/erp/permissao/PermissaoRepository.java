package com.traxup.tplug.erp.permissao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissaoRepository extends JpaRepository<Permissao, UUID> {
    Optional<Permissao> findByChave(String chave);
    boolean existsByChave(String chave);
}
