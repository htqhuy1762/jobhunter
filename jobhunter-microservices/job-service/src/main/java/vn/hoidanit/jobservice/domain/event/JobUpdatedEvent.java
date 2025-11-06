package vn.hoidanit.jobservice.domain.event;

import lombok.Getter;

import java.time.Instant;

/**
 * Domain Event: Job was updated
 */
@Getter
public class JobUpdatedEvent implements DomainEvent {

    private final Long jobId;
    private final String jobName;
    private final Instant occurredOn;

    public JobUpdatedEvent(Long jobId, String jobName) {
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
        return "JobUpdated";
    }

    @Override
    public String toString() {
        return String.format("JobUpdatedEvent[jobId=%d, jobName=%s, occurredOn=%s]",
            jobId, jobName, occurredOn);
    }
}


