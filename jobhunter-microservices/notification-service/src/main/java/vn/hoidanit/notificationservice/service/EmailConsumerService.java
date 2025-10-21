package vn.hoidanit.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import vn.hoidanit.notificationservice.config.RabbitMQConfig;
import vn.hoidanit.notificationservice.dto.EmailMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConsumerService {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void consumeEmailMessage(EmailMessage emailMessage) {
        try {
            log.info("Received email message from queue: {}", emailMessage);

            if (emailMessage.isHtml()) {
                emailService.sendHtmlEmail(
                    emailMessage.getTo(),
                    emailMessage.getSubject(),
                    emailMessage.getTemplate(),
                    emailMessage.getVariables()
                );
            } else {
                emailService.sendSimpleEmail(
                    emailMessage.getTo(),
                    emailMessage.getSubject(),
                    emailMessage.getTemplate()
                );
            }

            log.info("Email sent successfully to: {}", emailMessage.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", emailMessage.getTo(), e.getMessage(), e);
            throw e; // Re-throw to trigger retry mechanism
        }
    }
}

