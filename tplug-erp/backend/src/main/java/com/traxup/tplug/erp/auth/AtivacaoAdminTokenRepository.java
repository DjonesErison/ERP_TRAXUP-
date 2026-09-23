package com.traxup.tplug.erp.auth;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
public interface AtivacaoAdminTokenRepository extends JpaRepository<AtivacaoAdminToken,UUID> {
    Optional<AtivacaoAdminToken> findByTokenHash(String tokenHash);
    @Modifying
    @Query("update AtivacaoAdminToken t set t.usadoEm=:now where t.usuario.id=:usuarioId and t.usadoEm is null")
    void consumeAll(@Param("usuarioId") UUID usuarioId,@Param("now") Instant now);
}
