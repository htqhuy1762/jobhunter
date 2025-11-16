package vn.hoidanit.jobservice.dto;

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
public class JobCreatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long jobId;
    private String jobName;
    private Long companyId;
    private String companyName;
    private List<Long> skillIds;
    private List<String> skills;
    private String location;
    private Double salary;
    private String level;
}

