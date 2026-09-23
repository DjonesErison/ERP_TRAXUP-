package com.traxup.tplug.erp.trial.mail;

import com.traxup.tplug.erp.auth.AuthApplicationService;
import com.traxup.tplug.erp.trial.TrialSaas;
import com.traxup.tplug.erp.trial.TrialSaasRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Durable delivery intent. No plaintext activation token or password is persisted here. */
@Service
@Transactional
public class TrialEmailQueue {
    private final JdbcTemplate jdbc;
    private final TrialSaasRepository trials;
    private final AuthApplicationService auth;
    public TrialEmailQueue(JdbcTemplate jdbc, TrialSaasRepository trials, AuthApplicationService auth) {
        this.jdbc=jdbc; this.trials=trials; this.auth=auth;
    }
    public void enqueue(TrialSaas trial) {
        jdbc.update("INSERT INTO trial_activation_emails(trial_id) VALUES (?) ON CONFLICT DO NOTHING",trial.getId());
    }
    public void resend(UUID tenantId,String email) {
        trials.findByTenantIdAndEmailIgnoreCase(tenantId,email.trim()).filter(t -> !t.isAdminAtivado() && t.getExpiraEm().isAfter(Instant.now())).ifPresent(t -> {
            jdbc.update("INSERT INTO trial_activation_emails(trial_id,last_requested_at,requests_in_window) VALUES (?,NOW()-INTERVAL '1 minute',0) ON CONFLICT DO NOTHING",t.getId());
            // One minute cooldown, six requests/day, serialized by the UPDATE row lock.
            jdbc.update("""
                UPDATE trial_activation_emails SET status='PENDING', attempts=0, next_attempt_at=NOW(),
                  last_requested_at=NOW(), last_error=NULL,
                  requests_in_window=CASE WHEN window_started_at <= NOW()-INTERVAL '1 day' THEN 1 ELSE requests_in_window+1 END,
                  window_started_at=CASE WHEN window_started_at <= NOW()-INTERVAL '1 day' THEN NOW() ELSE window_started_at END
                WHERE trial_id=? AND status<>'SENDING' AND last_requested_at <= NOW()-INTERVAL '1 minute'
                  AND (requests_in_window<6 OR window_started_at <= NOW()-INTERVAL '1 day')
                """,t.getId());
        });
    }
    public record Delivery(UUID trialId,int attempt,String email,String name,UUID tenantId,Instant expiresAt,String token) {}
    public Optional<Delivery> prepare() {
        var ids=jdbc.query("""
            SELECT trial_id FROM trial_activation_emails
            WHERE status IN ('PENDING','SENDING') AND next_attempt_at <= NOW()
            ORDER BY next_attempt_at LIMIT 1 FOR UPDATE SKIP LOCKED
            """,(rs,row)->rs.getObject(1,UUID.class));
        if(ids.isEmpty()) return Optional.empty();
        UUID id=ids.getFirst();
        TrialSaas trial=trials.findById(id).orElseThrow();
        if(trial.isAdminAtivado() || !trial.getExpiraEm().isAfter(Instant.now()) || !trial.getAdministrador().isAtivo()) {
            jdbc.update("UPDATE trial_activation_emails SET status='CANCELLED' WHERE trial_id=?",id);
            return Optional.empty();
        }
        int attempt=jdbc.queryForObject("SELECT attempts FROM trial_activation_emails WHERE trial_id=?",Integer.class,id)+1;
        if(attempt>8) { jdbc.update("UPDATE trial_activation_emails SET status='FAILED' WHERE trial_id=?",id); return Optional.empty(); }
        String token=auth.criarAtivacaoAdministrador(trial.getAdministrador());
        jdbc.update("UPDATE trial_activation_emails SET status='SENDING', attempts=?, next_attempt_at=NOW()+INTERVAL '10 minutes' WHERE trial_id=?",attempt,id);
        // Transaction commits token hash and lease BEFORE the worker contacts SMTP.
        return Optional.of(new Delivery(id,attempt,trial.getAdministrador().getEmail(),trial.getAdministrador().getNome(),trial.getTenantId(),trial.getExpiraEm(),token));
    }
    public void sent(Delivery delivery) {
        jdbc.update("UPDATE trial_activation_emails SET status='SENT',sent_at=NOW(),last_error=NULL WHERE trial_id=? AND status='SENDING' AND attempts=?",delivery.trialId(),delivery.attempt());
    }
    public void failed(Delivery delivery) {
        int delay=Math.min(3600,30*(1 << Math.min(delivery.attempt(),7)));
        jdbc.update("UPDATE trial_activation_emails SET status=?,next_attempt_at=NOW()+(? * INTERVAL '1 second'),last_error='SMTP_DELIVERY_FAILED' WHERE trial_id=? AND status='SENDING' AND attempts=?",delivery.attempt()>=8?"FAILED":"PENDING",delay,delivery.trialId(),delivery.attempt());
    }
}
