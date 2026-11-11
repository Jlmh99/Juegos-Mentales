package com.mindgames.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envía el código de verificación (2FA) por correo con Brevo.
 *
 * SEGURIDAD: los logs NO incluyen el correo del destinatario, el código ni el
 * mensaje de la excepción (puede traer datos del servidor SMTP).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    /** Remitente verificado en Brevo (variable de entorno MAIL_FROM). */
    private final String remitente;

    public EmailService(JavaMailSender mailSender, @Value("${app.mail.from}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    public void enviarCodigo(String destino, String codigo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destino);
            mensaje.setSubject("Código de Verificación - Mind Games 🧠");
            mensaje.setText("Tu código de seguridad es: " + codigo + "\n\nExpira en 5 minutos.");

            mailSender.send(mensaje);
            log.info("Correo de verificación enviado");
        } catch (Exception e) {
            // Igual que antes: el error no se propaga, solo se registra (sin detalles).
            log.error("No se pudo enviar el correo de verificación ({})", e.getClass().getSimpleName());
        }
    }
}
