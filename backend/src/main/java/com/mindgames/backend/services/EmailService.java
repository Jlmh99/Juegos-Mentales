package com.mindgames.backend.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigo(String destino, String codigo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            
            // Debe ser el mismo de tu application.properties
            mensaje.setFrom("juanluismh1999@gmail.com"); 
            mensaje.setTo(destino);
            mensaje.setSubject("Código de Verificación - Mind Games 🧠");
            mensaje.setText("Tu código de seguridad es: " + codigo + "\n\nExpira en 5 minutos.");

            mailSender.send(mensaje);
            System.out.println("📧 Correo enviado a " + destino);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar correo: " + e.getMessage());
        }
    }
}
    

