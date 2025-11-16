package vn.hoidanit.jobservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hoidanit.jobservice.util.constant.LevelEnum;
import vn.hoidanit.jobservice.util.constant.LocationEnum;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for Job with Company information
 * Used for listing jobs with company logo
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResJobDTO {
    private long id;
    private String name;
    private LocationEnum location;  // Changed to enum
    private double salary;
    private int quantity;
    private LevelEnum level;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private List<SkillInfo> skills;  // Changed to object array with id

    // Company info for displaying logo
    private CompanyInfo company;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfo {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfo {
        private Long id;
        private String name;
        private String logo;
    }
}

