package vn.hoidanit.notificationservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.hoidanit.notificationservice.dto.JobAlertEvent;
import vn.hoidanit.notificationservice.service.EmailService;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobAlertEmailConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "job-alert-emails",
            groupId = "notification-service-job-alerts",
            containerFactory = "jobAlertEventKafkaListenerContainerFactory"
    )
    public void handleJobAlert(JobAlertEvent event) {
        log.info("Received job alert event: jobId={}, subscriberEmail={}",
                event.getJobId(), event.getSubscriberEmail());

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("name", event.getSubscriberName());
            variables.put("jobName", event.getJobName());
            variables.put("companyName", event.getCompanyName());
            variables.put("matchedSkills", event.getMatchedSkills());
            variables.put("jobId", event.getJobId());
            variables.put("location", event.getLocation());
            variables.put("salary", event.getSalary());

            emailService.sendHtmlEmail(
                    event.getSubscriberEmail(),
                    "New Job Alert: " + event.getJobName() + " at " + event.getCompanyName(),
                    "job-alert",  // Thymeleaf template name
                    variables
            );

            log.info("Job alert email sent successfully to: {}", event.getSubscriberEmail());
        } catch (Exception e) {
            log.error("Failed to send job alert email to {}: {}",
                    event.getSubscriberEmail(), e.getMessage(), e);
        }
    }
}

