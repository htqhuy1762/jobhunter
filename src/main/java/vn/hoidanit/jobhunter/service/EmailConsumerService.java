package vn.hoidanit.jobhunter.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.config.RabbitMQConfig;
import vn.hoidanit.jobhunter.domain.dto.EmailDTO;

@Service
@Slf4j
public class EmailConsumerService {

    private final EmailService emailService;
    private final EmailProducerService emailProducerService;
    private static final int MAX_RETRY_COUNT = 3;

    public EmailConsumerService(EmailService emailService, EmailProducerService emailProducerService) {
        this.emailService = emailService;
        this.emailProducerService = emailProducerService;
    }

    /**
     * Consumer chính - xử lý email từ queue
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void consumeEmailMessage(EmailDTO emailDTO,
                                   @Header(value = "retry-count", defaultValue = "0") int retryCount) {
        try {
            log.info("Processing email message for recipient: {}, retry count: {}",
                    emailDTO.getTo(), retryCount);

            // Gửi email thực tế
            emailService.sendEmailFromTemplateSync(
                emailDTO.getTo(),
                emailDTO.getSubject(),
                emailDTO.getTemplateName(),
                emailDTO.getUsername(),
                emailDTO.getValue()
            );

            log.info("Email sent successfully to: {}", emailDTO.getTo());

        } catch (Exception e) {
            log.error("Failed to send email to: {}, error: {}", emailDTO.getTo(), e.getMessage());

            // Retry logic
            if (retryCount < MAX_RETRY_COUNT) {
                log.warn("Retrying email send, attempt: {}/{}", retryCount + 1, MAX_RETRY_COUNT);
                emailProducerService.sendEmailToQueue(emailDTO, retryCount + 1);
            } else {
                log.error("Max retry attempts reached for email to: {}. Message will be sent to DLQ",
                         emailDTO.getTo());
                throw new RuntimeException("Max retry attempts reached", e);
            }
        }
    }

    /**
     * Consumer cho Dead Letter Queue - xử lý các email failed
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_DLQ)
    public void consumeDeadLetterQueue(EmailDTO emailDTO) {
        log.error("Email in DLQ - Failed permanently for recipient: {}", emailDTO.getTo());

        // Có thể implement logic để:
        // - Lưu vào database để admin review
        // - Gửi notification cho dev team
        // - Log vào monitoring system

        // TODO: Implement your DLQ handling logic here
        // Example: save to failed_emails table, send alert, etc.
    }
}
