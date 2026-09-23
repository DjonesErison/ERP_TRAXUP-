package com.traxup.tplug.erp.auth;

import com.traxup.tplug.erp.trial.TrialSaasRepository;
import com.traxup.tplug.erp.usuario.UsuarioRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Persist delivery intent only. Tokens are hashed by AuthApplicationService, never stored in this queue. */
@Service
@Transactional
public class AccessRecoveryQueue {
    private final JdbcTemplate jdbc;
    private final TrialSaasRepository trials;
    private final UsuarioRepository users;
    private final AuthApplicationService auth;
    public AccessRecoveryQueue(JdbcTemplate jdbc, TrialSaasRepository trials, UsuarioRepository users, AuthApplicationService auth) {
        this.jdbc=jdbc; this.trials=trials; this.users=users; this.auth=auth;
    }
    public void requestDocument(String document, String email) {
        trials.findByDocumentoAndEmailIgnoreCase(document.replaceAll("[.\\s/-]", ""),email.trim())
            .ifPresent(t -> requestTenant(t.getTenantId(), email));
    }
    public void requestTenant(UUID tenantId, String email) {
        users.findByTenantIdAndEmailIgnoreCase(tenantId,email.trim()).filter(u -> u.isAtivo() && u.getTenant().isAtivo()).ifPresent(u -> {
            jdbc.update("""
                INSERT INTO access_recovery_emails(usuario_id) VALUES (?)
                ON CONFLICT (usuario_id) DO UPDATE SET status='PENDING', attempts=0, next_attempt_at=NOW(), last_requested_at=NOW(),
                requests_in_window=CASE WHEN access_recovery_emails.window_started_at<=NOW()-INTERVAL '1 day' THEN 1 ELSE access_recovery_emails.requests_in_window+1 END,
                window_started_at=CASE WHEN access_recovery_emails.window_started_at<=NOW()-INTERVAL '1 day' THEN NOW() ELSE access_recovery_emails.window_started_at END
                WHERE access_recovery_emails.status<>'SENDING' AND access_recovery_emails.last_requested_at<=NOW()-INTERVAL '1 minute'
                AND (access_recovery_emails.requests_in_window<6 OR access_recovery_emails.window_started_at<=NOW()-INTERVAL '1 day')
                """,u.getId());
        });
    }
    public record Delivery(UUID userId, int attempt, String email, String code, String action, String token) {}
    public Optional<Delivery> prepare() {
        var ids=jdbc.query("""
            SELECT usuario_id FROM access_recovery_emails WHERE status IN ('PENDING','SENDING') AND next_attempt_at<=NOW()
            ORDER BY next_attempt_at LIMIT 1 FOR UPDATE SKIP LOCKED
            """,(rs,row)->rs.getObject(1,UUID.class));
        if(ids.isEmpty()) return Optional.empty();
        UUID id=ids.getFirst(); var user=users.findById(id).orElseThrow();
        int attempt=jdbc.queryForObject("SELECT attempts FROM access_recovery_emails WHERE usuario_id=?",Integer.class,id)+1;
        if(!user.isAtivo() || !user.getTenant().isAtivo() || attempt>8) {
            jdbc.update("UPDATE access_recovery_emails SET status='CANCELLED' WHERE usuario_id=?",id); return Optional.empty();
        }
        // Inspect the tenant's trial even when a non-administrator requests recovery.
        var trial=jdbc.query("SELECT status,expira_em,admin_ativado_em,administrador_id FROM trials_saas WHERE tenant_id=?",
            (rs,row)->new TrialState(rs.getString(1),rs.getTimestamp(2).toInstant(),rs.getTimestamp(3)!=null,rs.getObject(4,UUID.class)),user.getTenant().getId());
        String action="RECOVER", token=null;
        if(!trial.isEmpty()) {
            var t=trial.getFirst();
            if(!"CONVERTIDO".equals(t.status()) && (!"ATIVO".equals(t.status()) || !t.expires().isAfter(Instant.now()))) action="EXPIRED";
            else if(!t.active()) action=id.equals(t.admin()) ? "ACTIVATE" : "EXPIRED";
        }
        if(action.equals("ACTIVATE")) token=auth.criarAtivacaoAdministrador(user);
        if(action.equals("RECOVER")) token=auth.solicitarRecuperacao(user.getTenant().getId(),user.getEmail()).orElseThrow();
        jdbc.update("UPDATE access_recovery_emails SET status='SENDING', attempts=?, next_attempt_at=NOW()+INTERVAL '10 minutes' WHERE usuario_id=?",attempt,id);
        return Optional.of(new Delivery(id,attempt,user.getEmail(),user.getTenant().getCodigoEmpresa(),action,token));
    }
    private record TrialState(String status, Instant expires, boolean active, UUID admin) {}
    public void sent(Delivery d) { jdbc.update("UPDATE access_recovery_emails SET status='SENT' WHERE usuario_id=? AND attempts=? AND status='SENDING'",d.userId(),d.attempt()); }
    public void failed(Delivery d) {
        jdbc.update("UPDATE access_recovery_emails SET status=?, next_attempt_at=NOW()+INTERVAL '5 minutes' WHERE usuario_id=? AND attempts=? AND status='SENDING'",d.attempt()>=8?"FAILED":"PENDING",d.userId(),d.attempt());
    }
}
