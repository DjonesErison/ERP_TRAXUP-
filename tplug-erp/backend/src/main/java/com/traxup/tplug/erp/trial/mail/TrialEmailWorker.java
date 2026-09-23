package com.traxup.tplug.erp.trial.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="trial.mail.enabled",havingValue="true")
public class TrialEmailWorker {
    private static final Logger log=LoggerFactory.getLogger(TrialEmailWorker.class);
    private final TrialEmailQueue queue;
    private final TrialActivationMail mail;
    public TrialEmailWorker(TrialEmailQueue queue,TrialActivationMail mail) {this.queue=queue;this.mail=mail;}
    @Scheduled(fixedDelayString="${trial.mail.delay-ms:10000}",initialDelayString="${trial.mail.initial-delay-ms:15000}")
    public void deliver() {
        for(int i=0;i<10;i++) {
            var next=queue.prepare(); if(next.isEmpty()) return;
            var delivery=next.get();
            try { mail.send(delivery); }
            catch(MailException exception) {
                queue.failed(delivery);
                // Do not log SMTP exception messages, recipient addresses or activation URLs.
                log.warn("Trial activation email delivery failed; retry scheduled (attempt {})",delivery.attempt());
                continue;
            }
            queue.sent(delivery);
        }
    }
}
