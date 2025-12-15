package vn.hoidanit.notificationservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.hoidanit.notificationservice.config.KafkaConfig;
import vn.hoidanit.notificationservice.dto.EmailMessage;
import vn.hoidanit.notificationservice.service.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConsumerService {

    private final EmailService emailService;

    @KafkaListener(
        topics = KafkaConfig.EMAIL_TOPIC,
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeEmailMessage(EmailMessage emailMessage) {
        try {
            log.info("Received email message from Kafka topic: {}", emailMessage);

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
            // Kafka will automatically retry based on consumer configuration
            throw e;
        }
    }
}

