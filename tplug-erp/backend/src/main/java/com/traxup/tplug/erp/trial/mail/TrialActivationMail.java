package com.traxup.tplug.erp.trial.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class TrialActivationMail {
    private final JavaMailSender sender;
    private final String from;
    private final String publicUrl;
    public TrialActivationMail(JavaMailSender sender,@Value("${trial.mail.from}") String from,@Value("${trial.mail.public-url}") String publicUrl) {
        URI uri=URI.create(publicUrl);
        if(!"https".equals(uri.getScheme()) || uri.getHost()==null || uri.getRawQuery()!=null || uri.getRawFragment()!=null || uri.getUserInfo()!=null) throw new IllegalArgumentException("Trial public URL must use HTTPS without query/fragment");
        this.sender=sender;this.from=from;this.publicUrl=publicUrl.replaceAll("/+$","");
    }
    public SimpleMailMessage message(TrialEmailQueue.Delivery d) {
        String context="empresa="+d.tenantId()+"&email="+URLEncoder.encode(d.email(),StandardCharsets.UTF_8);
        String activation=publicUrl+"/ativar#token="+d.token()+"&"+context;
        String login=publicUrl+"/entrar#"+context;
        var message=new SimpleMailMessage();
        message.setFrom(from);message.setTo(d.email());
        message.setSubject("TRAXUP | Confirme seu cadastro e crie sua senha");
        message.setText("""
            Olá, %s!

            Seu cadastro na TRAXUP foi concluído. Para ativar o acesso do administrador, crie sua senha:

            %s

            Este link é pessoal, pode ser usado uma única vez e expira em até 24 horas, dentro do prazo do teste grátis.

            Seus dados de acesso:
            Empresa: %s
            E-mail: %s
            Acessar a plataforma: %s
            Seu teste grátis termina em: %s (horário de Brasília).

            Após criar a senha, entre para configurar sua empresa e começar a usar a TRAXUP.
            Se o link expirar, use a opção de reenviar ativação na página de acesso.
            Se você não solicitou este cadastro, ignore esta mensagem.

            Equipe TRAXUP
            Tecnologia que impulsiona negócios
            """.formatted(d.name(),activation,d.tenantId(),d.email(),login,DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").withZone(ZoneId.of("America/Recife")).format(d.expiresAt())));
        return message;
    }
    public void send(TrialEmailQueue.Delivery delivery) { sender.send(message(delivery)); }
}
