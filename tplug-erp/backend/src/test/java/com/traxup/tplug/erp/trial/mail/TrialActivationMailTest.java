package com.traxup.tplug.erp.trial.mail;

import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrialActivationMailTest {
    private TrialEmailQueue.Delivery delivery() {
        return new TrialEmailQueue.Delivery(UUID.randomUUID(),1,"ana+teste@example.test","Ana",UUID.randomUUID(),Instant.parse("2026-09-30T15:00:00Z"),"personal-token");
    }
    @Test void linkTemContextoEFragmentoSemSenha() {
        var sender=mock(JavaMailSender.class);
        var service=new TrialActivationMail(sender,"naoresponda@traxup.com.br","https://captacaoclientes.traxup.com.br/");
        var d=delivery(); var message=service.message(d);
        assertThat(message.getTo()).containsExactly(d.email());
        assertThat(message.getFrom()).isEqualTo("naoresponda@traxup.com.br");
        assertThat(message.getText()).contains("/ativar#token=personal-token&empresa="+d.tenantId(),"email=ana%2Bteste%40example.test","/entrar#empresa=", "30/09/2026 às 12:00", "crie sua senha").doesNotContain("Senha:","smtp-password");
        service.send(d);verify(sender).send(message);
    }
    @Test void rejeitaUrlInsegura() {
        assertThatThrownBy(()->new TrialActivationMail(mock(JavaMailSender.class),"from@example.test","http://example.test")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void falhaSmtpAgendaNovaTentativaSemMarcarEnviado() {
        var queue=mock(TrialEmailQueue.class);var mail=mock(TrialActivationMail.class);var d=delivery();
        when(queue.prepare()).thenReturn(Optional.of(d),Optional.empty());
        doThrow(new MailSendException("sensitive SMTP details")).when(mail).send(d);
        new TrialEmailWorker(queue,mail).deliver();
        verify(queue).failed(d);verify(queue,never()).sent(any());
    }
    @Test void confirmaEnvioSomenteDepoisDoSmtp() {
        var queue=mock(TrialEmailQueue.class);var mail=mock(TrialActivationMail.class);var d=delivery();
        when(queue.prepare()).thenReturn(Optional.of(d),Optional.empty());
        new TrialEmailWorker(queue,mail).deliver();
        var ordered=inOrder(queue,mail);ordered.verify(queue).prepare();ordered.verify(mail).send(d);ordered.verify(queue).sent(d);
    }
}
