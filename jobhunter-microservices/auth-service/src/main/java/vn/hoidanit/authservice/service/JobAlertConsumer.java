package vn.hoidanit.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.hoidanit.authservice.domain.Subscriber;
import vn.hoidanit.authservice.dto.JobAlertEvent;
import vn.hoidanit.authservice.dto.JobCreatedEvent;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobAlertConsumer {

    private final SubscriberService subscriberService;
    private final KafkaTemplate<String, JobAlertEvent> jobAlertKafkaTemplate;

    @KafkaListener(
            topics = "job-created",
            groupId = "auth-service-job-alerts",
            containerFactory = "jobCreatedKafkaListenerContainerFactory"
    )
    public void handleJobCreated(JobCreatedEvent event) {
        log.info("Received job-created event: jobId={}, jobName={}, skills={}",
                event.getJobId(), event.getJobName(), event.getSkills());

        if (event.getSkillIds() == null || event.getSkillIds().isEmpty()) {
            log.warn("Job {} has no skills, skipping subscriber matching", event.getJobId());
            return;
        }

        try {
            // Find subscribers matching these skills
            List<Subscriber> matchingSubscribers = subscriberService.findMatchingSubscribers(event.getSkillIds());

            log.info("Found {} subscribers matching skills for job {}",
                    matchingSubscribers.size(), event.getJobId());

            // Publish email alert event for each subscriber
            for (Subscriber subscriber : matchingSubscribers) {
                // Get matched skill names
                List<String> matchedSkills = subscriber.getSkills().stream()
                        .filter(skill -> event.getSkillIds().contains(skill.getId()))
                        .map(skill -> skill.getName())
                        .collect(Collectors.toList());

                JobAlertEvent alertEvent = JobAlertEvent.builder()
                        .subscriberEmail(subscriber.getEmail())
                        .subscriberName(subscriber.getName())
                        .jobId(event.getJobId())
                        .jobName(event.getJobName())
                        .companyName(event.getCompanyName())
                        .matchedSkills(matchedSkills)
                        .location(event.getLocation())
                        .salary(event.getSalary())
                        .build();

                jobAlertKafkaTemplate.send("job-alert-emails", alertEvent);
                log.info("Published job alert email for subscriber: {}", subscriber.getEmail());
            }

            log.info("Successfully published {} job alert emails for job {}",
                    matchingSubscribers.size(), event.getJobId());

        } catch (Exception e) {
            log.error("Failed to process job-created event: jobId={}, error={}",
                    event.getJobId(), e.getMessage(), e);
        }
    }
}

