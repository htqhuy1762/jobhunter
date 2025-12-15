package vn.hoidanit.jobservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobservice.dto.ResumeApplicationEvent;
import vn.hoidanit.jobservice.service.JobService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeApplicationEventConsumer {

    private final JobService jobService;

    @KafkaListener(
            topics = "job-applications",
            groupId = "job-service-resume-events",
            containerFactory = "resumeEventKafkaListenerContainerFactory"
    )
    public void consumeResumeApplicationEvent(ResumeApplicationEvent event) {
        log.info("Received resume application event: type={}, resumeId={}, jobId={}",
                event.getEventType(), event.getResumeId(), event.getJobId());

        try {
            switch (event.getEventType()) {
                case RESUME_SUBMITTED -> handleResumeSubmitted(event);
                case RESUME_WITHDRAWN -> handleResumeWithdrawn(event);
                default -> log.debug("Event type {} not handled by job-service", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process resume application event: eventId={}, error={}",
                    event.getEventId(), e.getMessage(), e);
        }
    }

    private void handleResumeSubmitted(ResumeApplicationEvent event) {
        log.info("Updating application count for jobId={}", event.getJobId());

        // In a real system, you might:
        // 1. Increment application count in Job entity
        // 2. Update job statistics
        // 3. Trigger notifications if quota reached
        // 4. Update search rankings

        log.info("Job {} received new application from user {}",
                event.getJobId(), event.getUserEmail());
    }

    private void handleResumeWithdrawn(ResumeApplicationEvent event) {
        log.info("Decrementing application count for jobId={}", event.getJobId());

        log.info("Application withdrawn for job {}", event.getJobId());
    }
}

