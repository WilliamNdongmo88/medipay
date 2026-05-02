package com.medipay.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailAttachment;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class BrevoService {
    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    @Value("${app.env.apiUrl}")
    private String apiUrl;

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    public void sendEmailWithAttachment(String to,
                                        String subject,
                                        String body,
                                        byte[] pdfBytes,
                                        String attachmentName) {
        try {
            // Configuration du client API
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKeyAuth.setApiKey(apiKey);

            // Instanciation de l’API TransactionalEmailsApi
            TransactionalEmailsApi emailApi = new TransactionalEmailsApi(defaultClient);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Créer le contexte pour le template
            Context context = new Context();
            context.setVariable("userName", "William");
            context.setVariable("title", "Rapport de transactions utilisateurs");
            context.setVariable("appName", senderName);

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("rapport-transaction", context);
            helper.setText(htmlContent, true);

            // L'API Brevo attend un byte[] Base64, pas un String.
            SendSmtpEmailAttachment brevoAttachment = new SendSmtpEmailAttachment();
            brevoAttachment.setName(attachmentName);
            brevoAttachment.setContent(pdfBytes);

            // Préparation de l'email
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(new SendSmtpEmailSender()
                    .email(senderEmail)
                    .name(senderName));
            sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(to)));
            sendSmtpEmail.setSubject(subject);
            sendSmtpEmail.setHtmlContent(htmlContent);

            sendSmtpEmail.setAttachment(Collections.singletonList(brevoAttachment));

            // Envoi de l'email
            emailApi.sendTransacEmail(sendSmtpEmail);
            System.out.println("✅ Email envoyé avec succès à " + to + " via Brevo.");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l’envoi du mail Brevo : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
