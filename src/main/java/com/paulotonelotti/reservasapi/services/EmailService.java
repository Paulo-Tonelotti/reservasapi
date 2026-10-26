package com.paulotonelotti.reservasapi.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.remetente}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarRedefinicaoSenha(String destinatario, String link) {
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(remetente);
            mensagem.setTo(destinatario);
            mensagem.setSubject("Redefinição de senha");
            mensagem.setText("""
            Recebemos um pedido para redefinir a sua senha.
    
            Acesse o link abaixo (válido por 30 minutos):
            %s
    
            Se não foi você, ignore este e-mail.
            """.formatted(link));

            mailSender.send(mensagem);
        } catch (MailException ex) {
            log.error("Falha ao enviar e-mail (destino={}): {}", mascarar(destinatario), ex.getMessage(), ex);
        }
    }

    private String mascarar(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
