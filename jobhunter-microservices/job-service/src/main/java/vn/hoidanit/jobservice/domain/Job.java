package vn.hoidanit.jobservice.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hoidanit.jobservice.domain.event.*;
import vn.hoidanit.jobservice.domain.valueobject.DateRange;
import vn.hoidanit.jobservice.domain.valueobject.Location;
import vn.hoidanit.jobservice.domain.valueobject.Salary;
import vn.hoidanit.jobservice.util.SecurityUtil;
import vn.hoidanit.jobservice.util.constant.LevelEnum;

/**
 * Job Aggregate Root in DDD
 * Contains business logic and maintains consistency of the aggregate
 * Raises domain events for important business changes
 */
@Entity
@Getter
@Table(name = "jobs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Job extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    // Legacy field for backward compatibility - will migrate to Location value object
    @Setter
    private String location;

    // Legacy field for backward compatibility - will migrate to Salary value object
    @Setter
    private double salary;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private LevelEnum level;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    // Legacy fields for backward compatibility - will migrate to DateRange value object
    @Setter
    private Instant startDate;
    @Setter
    private Instant endDate;

    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // Reference to Company service (microservices pattern)
    private Long companyId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "jobs" })
    @JoinTable(name = "job_skill",
               joinColumns = @JoinColumn(name = "job_id"),
               inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> skills = new ArrayList<>();

    // ==================== FACTORY METHOD (DDD Pattern) ====================

    /**
     * Factory method to create a new Job (DDD pattern)
     * Encapsulates creation logic and ensures invariants
     */
    public static Job create(String name, String location, double salary,
                            int quantity, LevelEnum level, String description,
                            Instant startDate, Instant endDate, Long companyId) {
        Job job = new Job();
        job.name = name;
        job.location = location;
        job.salary = salary;
        job.quantity = quantity;
        job.level = level;
        job.description = description;
        job.startDate = startDate;
        job.endDate = endDate;
        job.companyId = companyId;
        job.active = false; // New jobs start as inactive
        job.skills = new ArrayList<>();

        // Validate business rules
        job.validateInvariants();

        return job;
    }

    // ==================== BUSINESS LOGIC (Rich Domain Model) ====================

    /**
     * Publish the job - business operation with rules
     */
    public void publish() {
        validateCanBePublished();

        if (this.active) {
            throw new IllegalStateException("Job is already published");
        }

        this.active = true;

        // Raise domain event
        registerEvent(new JobPublishedEvent(this.id, this.name));
    }

    /**
     * Close/deactivate the job
     */
    public void close(String reason) {
        if (!this.active) {
            throw new IllegalStateException("Job is already closed");
        }

        this.active = false;

        // Raise domain event
        registerEvent(new JobClosedEvent(this.id, this.name, reason));
    }

    /**
     * Update job information - maintains consistency
     */
    public void updateInformation(String name, String location, double salary,
                                  int quantity, LevelEnum level, String description) {
        this.name = name;
        this.location = location;
        this.salary = salary;
        this.quantity = quantity;
        this.level = level;
        this.description = description;

        validateInvariants();

        // Raise domain event
        registerEvent(new JobUpdatedEvent(this.id, this.name));
    }

    /**
     * Add a required skill
     */
    public void addRequiredSkill(Skill skill) {
        if (skill == null) {
            throw new IllegalArgumentException("Skill cannot be null");
        }

        if (this.skills.contains(skill)) {
            return; // Already has this skill
        }

        this.skills.add(skill);
    }

    /**
     * Remove a required skill
     */
    public void removeRequiredSkill(Skill skill) {
        this.skills.remove(skill);
    }

    /**
     * Set all required skills at once
     */
    public void setRequiredSkills(List<Skill> skills) {
        if (skills == null) {
            this.skills = new ArrayList<>();
        } else {
            this.skills = new ArrayList<>(skills);
        }
    }

    /**
     * Get immutable list of skills
     */
    public List<Skill> getRequiredSkills() {
        return Collections.unmodifiableList(skills);
    }

    /**
     * Extend the job posting period
     */
    public void extendDeadline(Instant newEndDate) {
        if (newEndDate == null) {
            throw new IllegalArgumentException("New end date cannot be null");
        }

        if (newEndDate.isBefore(this.endDate)) {
            throw new IllegalArgumentException("New end date must be after current end date");
        }

        this.endDate = newEndDate;
    }

    /**
     * Check if the job is still accepting applications
     */
    public boolean isAcceptingApplications() {
        Instant now = Instant.now();
        return this.active &&
               !now.isBefore(this.startDate) &&
               !now.isAfter(this.endDate);
    }

    /**
     * Check if the job has expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.endDate);
    }

    // ==================== VALIDATION (Invariants) ====================

    /**
     * Validate business invariants/rules
     */
    private void validateInvariants() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Job name is required");
        }

        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Job location is required");
        }

        if (salary < 0) {
            throw new IllegalArgumentException("Salary must be positive");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        if (companyId == null) {
            throw new IllegalArgumentException("Company ID is required");
        }
    }

    /**
     * Validate if job can be published
     */
    private void validateCanBePublished() {
        validateInvariants();

        if (this.skills.isEmpty()) {
            throw new IllegalStateException("Cannot publish job without required skills");
        }

        if (this.description == null || this.description.trim().isEmpty()) {
            throw new IllegalStateException("Cannot publish job without description");
        }

        Instant now = Instant.now();
        if (this.endDate.isBefore(now)) {
            throw new IllegalStateException("Cannot publish job with expired end date");
        }
    }

    // ==================== JPA LIFECYCLE CALLBACKS ====================

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdAt = Instant.now();

        // Validate before persisting
        validateInvariants();

        // Raise creation event
        registerEvent(new JobCreatedEvent(this.id, this.name, this.companyId));
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.updatedAt = Instant.now();

        // Validate before updating
        validateInvariants();
    }

    // ==================== LEGACY SETTERS (for backward compatibility) ====================
    // These will be gradually phased out as we migrate to proper domain operations

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setLevel(LevelEnum level) {
        this.level = level;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills != null ? skills : new ArrayList<>();
    }

    public void setId(long id) {
        this.id = id;
    }
}