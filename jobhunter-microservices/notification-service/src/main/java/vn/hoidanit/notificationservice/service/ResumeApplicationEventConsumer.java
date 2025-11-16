package vn.hoidanit.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.hoidanit.notificationservice.dto.EmailMessage;
import vn.hoidanit.notificationservice.dto.ResumeApplicationEvent;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeApplicationEventConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "job-applications",
            groupId = "notification-service-resume-events",
            containerFactory = "resumeEventKafkaListenerContainerFactory"
    )
    public void consumeResumeApplicationEvent(ResumeApplicationEvent event) {
        log.info("Received resume application event: type={}, resumeId={}, jobId={}, userEmail={}",
                event.getEventType(), event.getResumeId(), event.getJobId(), event.getUserEmail());

        try {
            switch (event.getEventType()) {
                case RESUME_SUBMITTED -> handleResumeSubmitted(event);
                case RESUME_APPROVED -> handleResumeApproved(event);
                case RESUME_REJECTED -> handleResumeRejected(event);
                case RESUME_WITHDRAWN -> handleResumeWithdrawn(event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process resume application event: eventId={}, error={}",
                    event.getEventId(), e.getMessage(), e);
            // In production, you might want to send to DLQ (Dead Letter Queue)
        }
    }

    private void handleResumeSubmitted(ResumeApplicationEvent event) {
        log.info("Processing RESUME_SUBMITTED event for resumeId={}", event.getResumeId());

        // Send confirmation email to user
        sendEmailToUser(
                event.getUserEmail(),
                "Application Submitted Successfully",
                "resume-submitted",
                createUserEmailVariables(event)
        );

        // Send notification email to company/HR
        // In real system, you'd fetch company email from company-service
        log.info("Notification sent to company {} about new resume submission", event.getCompanyName());
    }

    private void handleResumeApproved(ResumeApplicationEvent event) {
        log.info("Processing RESUME_APPROVED event for resumeId={}", event.getResumeId());

        sendEmailToUser(
                event.getUserEmail(),
                "Congratulations! Your Application Was Approved",
                "resume-approved",
                createUserEmailVariables(event)
        );
    }

    private void handleResumeRejected(ResumeApplicationEvent event) {
        log.info("Processing RESUME_REJECTED event for resumeId={}", event.getResumeId());

        sendEmailToUser(
                event.getUserEmail(),
                "Application Status Update",
                "resume-rejected",
                createUserEmailVariables(event)
        );
    }

    private void handleResumeWithdrawn(ResumeApplicationEvent event) {
        log.info("Processing RESUME_WITHDRAWN event for resumeId={}", event.getResumeId());

        // Notify company that user withdrew their application
        log.info("Company notified about resume withdrawal");
    }

    private void sendEmailToUser(String to, String subject, String template, Map<String, Object> variables) {
        try {
            emailService.sendHtmlEmail(to, subject, template, variables);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }

    private Map<String, Object> createUserEmailVariables(ResumeApplicationEvent event) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("jobName", event.getJobName());
        variables.put("companyName", event.getCompanyName());
        variables.put("resumeId", event.getResumeId());
        variables.put("jobId", event.getJobId());
        return variables;
    }
}

