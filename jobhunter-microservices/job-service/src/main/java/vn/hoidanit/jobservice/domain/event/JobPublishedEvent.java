package vn.hoidanit.jobservice.domain.event;

import lombok.Getter;

import java.time.Instant;

/**
 * Domain Event: Job was published/activated
 * Important for notifying subscribers, updating search indexes, etc.
 */
@Getter
public class JobPublishedEvent implements DomainEvent {

    private final Long jobId;
    private final String jobName;
    private final Instant occurredOn;

    public JobPublishedEvent(Long jobId, String jobName) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.occurredOn = Instant.now();
    }

    @Override
    public Instant occurredOn() {
        return this.occurredOn;
    }

    @Override
    public String eventType() {
        return "JobPublished";
    }

    @Override
    public String toString() {
        return String.format("JobPublishedEvent[jobId=%d, jobName=%s, occurredOn=%s]",
            jobId, jobName, occurredOn);
    }
}

