package com.traxup.tplug.erp.auth;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
@ConditionalOnProperty(name="trial.mail.enabled",havingValue="true")
public class AccessRecoveryWorker {
    private final AccessRecoveryQueue queue; private final AccessRecoveryMail mail;
    public AccessRecoveryWorker(AccessRecoveryQueue queue, AccessRecoveryMail mail) {this.queue=queue; this.mail=mail;}
    @Scheduled(fixedDelayString="${trial.mail.delay-ms:10000}",initialDelayString="${trial.mail.initial-delay-ms:15000}")
    public void deliver() {
        for(int i=0;i<10;i++) {
            var next=queue.prepare(); if(next.isEmpty()) return; var d=next.get();
            try {mail.send(d);} catch(org.springframework.mail.MailException ex) {queue.failed(d); continue;}
            queue.sent(d);
        }
    }
}
