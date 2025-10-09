package vn.hoidanit.jobhunter.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.config.RabbitMQConfig;
import vn.hoidanit.jobhunter.domain.dto.EmailDTO;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailProducerService {
    private final RabbitTemplate rabbitTemplate;


    /**
     * Gửi email message vào RabbitMQ queue
     */
    public void sendEmailToQueue(String to, String subject, String templateName, String username, Object value) {
        try {
            EmailDTO emailDTO = new EmailDTO(to, subject, templateName, username, value);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    emailDTO
            );

            log.info("Email message sent to queue for recipient: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email message to queue: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send email to queue", e);
        }
    }

    /**
     * Gửi email với retry count (để tracking số lần retry)
     */
    public void sendEmailToQueue(EmailDTO emailDTO, int retryCount) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    emailDTO,
                    message -> {
                        message.getMessageProperties().setHeader("retry-count", retryCount);
                        return message;
                    }
            );
            log.info("Email message sent to queue with retry count: {}", retryCount);
        } catch (Exception e) {
            log.error("Failed to send email message to queue: {}", e.getMessage(), e);
        }
    }
}