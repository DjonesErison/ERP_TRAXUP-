package com.traxup.tplug.erp.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class AccessRecoveryMail {
    private final JavaMailSender sender;
    private final String from, url;
    public AccessRecoveryMail(JavaMailSender sender,@Value("${trial.mail.from}") String from,@Value("${trial.mail.public-url}") String url) {
        URI uri=URI.create(url);
        if(!"https".equals(uri.getScheme()) || uri.getHost()==null || uri.getRawQuery()!=null || uri.getRawFragment()!=null || uri.getUserInfo()!=null) throw new IllegalArgumentException("Invalid public URL");
        this.sender=sender; this.from=from; this.url=url.replaceAll("/+$","");
    }
    public SimpleMailMessage message(AccessRecoveryQueue.Delivery d) {
        var message=new SimpleMailMessage(); message.setFrom(from); message.setTo(d.email());
        message.setSubject("TRAXUP | Recuperação de acesso");
        String text;
        if(d.action().equals("EXPIRED")) text="Seu período de teste terminou ou está indisponível. Para reativar ou contratar a TRAXUP, entre em contato com nossa equipe comercial pelo site "+url+". Não é necessário criar outro cadastro.";
        else {
            String path=d.action().equals("ACTIVATE")?"/ativar":"/recuperar";
            String link=url+path+"#token="+d.token()+"&empresa="+d.code()+"&email="+URLEncoder.encode(d.email(),StandardCharsets.UTF_8);
            text="Para "+(d.action().equals("ACTIVATE")?"ativar seu cadastro":"redefinir sua senha")+", acesse:\n"+link+"\n\nLink pessoal, de uso único, válido por 30 minutos.";
        }
        message.setText(text+"\n\nEmpresa: "+d.code()+"\nE-mail: "+d.email()+"\nNunca enviamos sua senha existente. Se não solicitou este acesso, ignore a mensagem.\nEquipe TRAXUP");
        return message;
    }
    public void send(AccessRecoveryQueue.Delivery d) { sender.send(message(d)); }
}
