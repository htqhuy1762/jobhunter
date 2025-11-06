package vn.hoidanit.jobservice.domain.event;

import lombok.Getter;

import java.time.Instant;

/**
 * Domain Event: Job was created
 * This is an important business event that other parts of the system might need to react to
 */
@Getter
public class JobCreatedEvent implements DomainEvent {

    private final Long jobId;
    private final String jobName;
    private final Long companyId;
    private final Instant occurredOn;

    public JobCreatedEvent(Long jobId, String jobName, Long companyId) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.companyId = companyId;
        this.occurredOn = Instant.now();
    }

    @Override
    public Instant occurredOn() {
        return this.occurredOn;
    }

    @Override
    public String eventType() {
        return "JobCreated";
    }

    @Override
    public String toString() {
        return String.format("JobCreatedEvent[jobId=%d, jobName=%s, occurredOn=%s]",
            jobId, jobName, occurredOn);
    }
}

