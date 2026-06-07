package com.frauddetection.alert.service;

import com.frauddetection.alert.template.EmailTemplate;
import com.frauddetection.common.event.DecisionEvent;
import com.frauddetection.common.model.Action;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final EmailTemplate emailTemplate;

    @Value("${alerts.enabled:true}")
    private boolean alertsEnabled;

    @Value("${alerts.recipients.email:fraud-team@company.com}")
    private String alertRecipients;

    public void sendFraudAlert(DecisionEvent event) {
        if (!alertsEnabled) {
            log.debug("Alerts disabled, skipping notification for transaction: {}", event.getTransactionId());
            return;
        }
        
        try {
            String subject = "Fraud Alert: " + event.getAction() + " - Card " + event.getCardLast4();
            String body = emailTemplate.buildAlertBody(event);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(alertRecipients);
            helper.setSubject(subject);
            helper.setText(body, true);
            
            mailSender.send(message);
            log.info("Fraud alert email sent for transaction: {}", event.getTransactionId());
        } catch (MessagingException e) {
            log.error("Failed to send fraud alert email: {}", e.getMessage());
        }
    }

    public void sendSms(String phoneNumber, String message) {
        log.info("SMS to {}: {}", phoneNumber, message);
    }
}