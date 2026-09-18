package com.traxup.tplug.erp.auth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; import java.util.UUID;
public interface AtivacaoAdminTokenRepository extends JpaRepository<AtivacaoAdminToken,UUID>{ Optional<AtivacaoAdminToken> findByTokenHash(String tokenHash); }
