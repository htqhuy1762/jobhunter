package vn.hoidanit.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAlertEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String subscriberEmail;
    private String subscriberName;
    private Long jobId;
    private String jobName;
    private String companyName;
    private List<String> matchedSkills;
    private String location;
    private Double salary;
}

