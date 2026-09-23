package com.traxup.tplug.erp.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecuperacaoSenhaTokenRepository extends JpaRepository<RecuperacaoSenhaToken, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<RecuperacaoSenhaToken> findByTokenHash(String tokenHash);
}
