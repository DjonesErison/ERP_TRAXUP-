package com.traxup.tplug.erp.trial.mail;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TrialMailSchedulingTest {
    @Test void enviaAutomaticamenteSemModulosFiscalOuSped() {
        var queue = mock(TrialEmailQueue.class);
        var mail = mock(TrialActivationMail.class);
        var delivery = new TrialEmailQueue.Delivery(java.util.UUID.randomUUID(), 1,
                "test@example.test", "Test", java.util.UUID.randomUUID(), java.time.Instant.now(), "test-token");
        when(queue.prepare()).thenReturn(Optional.of(delivery), Optional.empty());
        new ApplicationContextRunner()
                .withUserConfiguration(TrialMailSchedulingConfiguration.class, TrialEmailWorker.class)
                .withBean(TrialEmailQueue.class, () -> queue)
                .withBean(TrialActivationMail.class, () -> mail)
                .withPropertyValues("trial.mail.enabled=true", "trial.mail.initial-delay-ms=0",
                        "trial.mail.delay-ms=60000", "fiscal.storage.enabled=false", "contabilidade.sped.worker.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(TrialEmailWorker.class);
                    // Exercise the real Spring scheduler rather than calling deliver() directly.
                    verify(queue, timeout(3000)).sent(delivery);
                    verify(mail).send(delivery);
                });
    }

    @Test void naoAgendaQuandoEnvioEstaDesativado() {
        new ApplicationContextRunner()
                .withUserConfiguration(TrialMailSchedulingConfiguration.class, TrialEmailWorker.class)
                .withPropertyValues("trial.mail.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TrialMailSchedulingConfiguration.class);
                    assertThat(context).doesNotHaveBean(TrialEmailWorker.class);
                });
    }
}
