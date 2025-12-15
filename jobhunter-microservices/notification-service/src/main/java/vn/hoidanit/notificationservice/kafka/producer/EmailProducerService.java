package vn.hoidanit.notificationservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import vn.hoidanit.notificationservice.config.KafkaConfig;
import vn.hoidanit.notificationservice.dto.EmailMessage;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProducerService {

    private final KafkaTemplate<String, EmailMessage> kafkaTemplate;

    public void sendEmailToQueue(EmailMessage emailMessage) {
        try {
            log.info("Sending email message to Kafka topic: {}", emailMessage);

            CompletableFuture<SendResult<String, EmailMessage>> future =
                kafkaTemplate.send(KafkaConfig.EMAIL_TOPIC, emailMessage);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Email message sent successfully to topic [{}] with offset [{}]",
                        KafkaConfig.EMAIL_TOPIC,
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send email message to Kafka: {}", ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            log.error("Failed to send email message to Kafka: {}", e.getMessage(), e);
            throw e;
        }
    }
}

