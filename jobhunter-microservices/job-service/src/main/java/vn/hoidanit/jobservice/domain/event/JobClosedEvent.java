package vn.hoidanit.jobservice.domain.event;

import lombok.Getter;

import java.time.Instant;

/**
 * Domain Event: Job was closed/deactivated
 * Important for stopping notifications, updating metrics, etc.
 */
@Getter
public class JobClosedEvent implements DomainEvent {

    private final Long jobId;
    private final String jobName;
    private final String reason;
    private final Instant occurredOn;

    public JobClosedEvent(Long jobId, String jobName, String reason) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.reason = reason;
        this.occurredOn = Instant.now();
    }

    public JobClosedEvent(Long jobId, String jobName) {
        this(jobId, jobName, "Closed by user");
    }

    @Override
    public Instant occurredOn() {
        return this.occurredOn;
    }

    @Override
    public String eventType() {
        return "JobClosed";
    }

    @Override
    public String toString() {
        return String.format("JobClosedEvent[jobId=%d, jobName=%s, reason=%s, occurredOn=%s]",
            jobId, jobName, reason, occurredOn);
    }
}

