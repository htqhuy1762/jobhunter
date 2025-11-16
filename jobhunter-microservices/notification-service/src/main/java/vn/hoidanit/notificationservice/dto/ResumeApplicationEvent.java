package vn.hoidanit.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeApplicationEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private Instant timestamp;
    private EventType eventType;

    private Long resumeId;
    private Long jobId;
    private Long userId;
    private Long companyId;

    private String userEmail;
    private String jobName;
    private String companyName;
    private String resumeUrl;

    public enum EventType {
        RESUME_SUBMITTED,
        RESUME_APPROVED,
        RESUME_REJECTED,
        RESUME_WITHDRAWN
    }
}

