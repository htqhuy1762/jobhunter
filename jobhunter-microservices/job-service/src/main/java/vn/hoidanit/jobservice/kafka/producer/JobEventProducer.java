package vn.hoidanit.jobservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobservice.config.KafkaProducerConfig;
import vn.hoidanit.jobservice.domain.Job;
import vn.hoidanit.jobservice.domain.Skill;
import vn.hoidanit.jobservice.dto.JobCreatedEvent;
import vn.hoidanit.jobservice.service.CompanyFetchService;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobEventProducer {

    private final KafkaTemplate<String, JobCreatedEvent> jobCreatedKafkaTemplate;
    private final CompanyFetchService companyFetchService;

    public void publishJobCreated(Job job) {
        try {
            // Fetch company name if not already loaded
            String companyName = "Unknown Company";
            if (job.getCompanyId() != null) {
                try {
                    var companyInfo = companyFetchService.fetchCompany(job.getCompanyId());
                    if (companyInfo != null) {
                        companyName = companyInfo.getName();
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch company name for jobId={}, using default", job.getId());
                }
            }

            JobCreatedEvent event = JobCreatedEvent.builder()
                    .jobId(job.getId())
                    .jobName(job.getName())
                    .companyId(job.getCompanyId())
                    .companyName(companyName)
                    .skillIds(job.getSkills() != null ?
                            job.getSkills().stream().map(Skill::getId).collect(Collectors.toList()) : null)
                    .skills(job.getSkills() != null ?
                            job.getSkills().stream().map(Skill::getName).collect(Collectors.toList()) : null)
                    .location(job.getLocation() != null ? job.getLocation().name() : null)  // Convert enum to string
                    .salary(job.getSalary())
                    .level(job.getLevel() != null ? job.getLevel().name() : null)
                    .build();

            // Use companyId as key for partitioning
            String key = String.valueOf(job.getCompanyId());

            CompletableFuture<SendResult<String, JobCreatedEvent>> future =
                    jobCreatedKafkaTemplate.send(KafkaProducerConfig.JOB_CREATED_TOPIC, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Published job-created event: jobId={}, jobName={}, partition={}",
                            event.getJobId(), event.getJobName(), result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to publish job-created event: jobId={}, jobName={}",
                            event.getJobId(), event.getJobName(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing job-created event for jobId={}: {}", job.getId(), e.getMessage(), e);
        }
    }
}

