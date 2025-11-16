package vn.hoidanit.resumeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import vn.hoidanit.resumeservice.config.KafkaProducerConfig;
import vn.hoidanit.resumeservice.dto.ResumeApplicationEvent;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeEventProducer {

    private final KafkaTemplate<String, ResumeApplicationEvent> resumeEventKafkaTemplate;

    public void publishResumeSubmittedEvent(ResumeApplicationEvent event) {
        try {
            // Set event metadata
            event.setEventId(UUID.randomUUID().toString());
            event.setTimestamp(Instant.now());
            event.setEventType(ResumeApplicationEvent.EventType.RESUME_SUBMITTED);

            // Use jobId as key for partitioning - ensures all events for same job go to same partition
            String key = String.valueOf(event.getJobId());

            CompletableFuture<SendResult<String, ResumeApplicationEvent>> future =
                    resumeEventKafkaTemplate.send(KafkaProducerConfig.JOB_APPLICATIONS_TOPIC, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Published RESUME_SUBMITTED event: resumeId={}, jobId={}, partition={}",
                            event.getResumeId(), event.getJobId(), result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to publish RESUME_SUBMITTED event: resumeId={}, jobId={}",
                            event.getResumeId(), event.getJobId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing resume event: {}", e.getMessage(), e);
        }
    }

    public void publishResumeStatusChangeEvent(ResumeApplicationEvent event) {
        try {
            event.setEventId(UUID.randomUUID().toString());
            event.setTimestamp(Instant.now());

            String key = String.valueOf(event.getJobId());

            CompletableFuture<SendResult<String, ResumeApplicationEvent>> future =
                    resumeEventKafkaTemplate.send(KafkaProducerConfig.JOB_APPLICATIONS_TOPIC, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Published {} event: resumeId={}, jobId={}",
                            event.getEventType(), event.getResumeId(), event.getJobId());
                } else {
                    log.error("Failed to publish {} event: resumeId={}, jobId={}",
                            event.getEventType(), event.getResumeId(), event.getJobId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing resume status change event: {}", e.getMessage(), e);
        }
    }
}

