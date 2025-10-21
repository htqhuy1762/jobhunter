package vn.hoidanit.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import vn.hoidanit.notificationservice.config.RabbitMQConfig;
import vn.hoidanit.notificationservice.dto.EmailMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProducerService {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmailToQueue(EmailMessage emailMessage) {
        try {
            log.info("Sending email message to queue: {}", emailMessage);
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                emailMessage
            );
            log.info("Email message sent to queue successfully");
        } catch (Exception e) {
            log.error("Failed to send email message to queue: {}", e.getMessage(), e);
            throw e;
        }
    }
}

