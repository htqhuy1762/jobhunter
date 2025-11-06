package vn.hoidanit.jobservice.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobservice.domain.Job;
import vn.hoidanit.jobservice.domain.Skill;
import vn.hoidanit.jobservice.dto.ResCreateJobDTO;
import vn.hoidanit.jobservice.dto.ResJobDTO;
import vn.hoidanit.jobservice.dto.ResUpdateJobDTO;
import vn.hoidanit.jobservice.dto.ResultPaginationDTO;
import vn.hoidanit.jobservice.infrastructure.event.DomainEventPublisher;
import vn.hoidanit.jobservice.repository.JobRepository;
import vn.hoidanit.jobservice.repository.SkillRepository;

/**
 * Application Service for Job operations
 * Orchestrates domain operations and publishes domain events
 * Now following DDD patterns with event publishing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final CompanyFetchService companyFetchService;
    private final DomainEventPublisher eventPublisher;

    /**
     * Create a new job (DDD pattern with domain events)
     */
    @Transactional
    public ResCreateJobDTO create(Job j) {
        // Check Skill
        if (j.getSkills() != null) {
            List<Long> reqSkills = j.getSkills().stream().map(x -> x.getId()).collect(Collectors.toList());
            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            j.setSkills(dbSkills);
        }

        // In microservices, companyId is already set, no need to fetch Company entity
        // The relationship is handled via service communication

        // Create job - domain events will be registered in @PrePersist
        Job currentJob = this.jobRepository.save(j);

        // Publish domain events (DDD pattern)
        eventPublisher.publishAll(currentJob.getDomainEvents());
        currentJob.clearDomainEvents();

        log.info("Job created with ID: {} - Domain events published", currentJob.getId());

        // Convert to response
        ResCreateJobDTO dto = new ResCreateJobDTO();
        dto.setId(currentJob.getId());
        dto.setName(currentJob.getName());
        dto.setLocation(currentJob.getLocation());
        dto.setSalary(currentJob.getSalary());
        dto.setQuantity(currentJob.getQuantity());
        dto.setLevel(currentJob.getLevel());
        dto.setStartDate(currentJob.getStartDate());
        dto.setEndDate(currentJob.getEndDate());
        dto.setActive(currentJob.isActive());
        dto.setCreatedBy(currentJob.getCreatedBy());
        dto.setCreatedAt(currentJob.getCreatedAt());

        if(currentJob.getSkills() != null) {
            dto.setSkills(currentJob.getSkills().stream().map(x -> x.getName()).collect(Collectors.toList()));
        }

        return dto;
    }

    public Optional<Job> fetchJobById(long id) {
        return this.jobRepository.findById(id);
    }

    /**
     * Fetch job by ID with company information for display
     */
    public ResJobDTO fetchJobByIdWithCompany(long id) {
        Optional<Job> jobOptional = this.jobRepository.findById(id);
        if (!jobOptional.isPresent()) {
            return null;
        }
        return convertToResJobDTO(jobOptional.get());
    }

    /**
     * Update a job (DDD pattern with domain events)
     */
    @Transactional
    public ResUpdateJobDTO update(Job j, Job jobInDB) {
        // check skills
        if(j.getSkills() != null) {
            List<Long> reqSkills = j.getSkills().stream().map(x -> x.getId()).collect(Collectors.toList());
            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            jobInDB.setSkills(dbSkills);
        }

        // Use domain method to update (registers domain event)
        jobInDB.updateInformation(
            j.getName(),
            j.getLocation(),
            j.getSalary(),
            j.getQuantity(),
            j.getLevel(),
            j.getDescription()
        );

        jobInDB.setStartDate(j.getStartDate());
        jobInDB.setEndDate(j.getEndDate());
        jobInDB.setActive(j.isActive());
        if(j.getCompanyId() != null) {
            jobInDB.setCompanyId(j.getCompanyId());
        }

        // Save job
        Job currentJob = this.jobRepository.save(jobInDB);

        // Publish domain events (DDD pattern)
        eventPublisher.publishAll(currentJob.getDomainEvents());
        currentJob.clearDomainEvents();

        log.info("Job updated with ID: {} - Domain events published", currentJob.getId());

        // Convert to response
        ResUpdateJobDTO dto = new ResUpdateJobDTO();
        dto.setId(currentJob.getId());
        dto.setName(currentJob.getName());
        dto.setLocation(currentJob.getLocation());
        dto.setSalary(currentJob.getSalary());
        dto.setQuantity(currentJob.getQuantity());
        dto.setLevel(currentJob.getLevel());
        dto.setStartDate(currentJob.getStartDate());
        dto.setEndDate(currentJob.getEndDate());
        dto.setActive(currentJob.isActive());
        dto.setUpdatedBy(currentJob.getUpdatedBy());
        dto.setUpdatedAt(currentJob.getUpdatedAt());

        if(currentJob.getSkills() != null) {
            dto.setSkills(currentJob.getSkills().stream().map(x -> x.getName()).collect(Collectors.toList()));
        }

        return dto;
    }

    public void delete(long id) {
        this.jobRepository.deleteById(id);
    }

    public ResultPaginationDTO fetchAll(Specification<Job> spec, Pageable pageable) {
        Page<Job> pageJob = this.jobRepository.findAll(spec, pageable);

        // Enrich jobs with company data
        List<vn.hoidanit.jobservice.dto.ResJobDTO> enrichedJobs = pageJob.getContent().stream()
                .map(this::convertToResJobDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageJob.getTotalPages());
        mt.setTotal(pageJob.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(enrichedJobs);

        return rs;
    }

    /**
     * Convert Job entity to ResJobDTO with company information
     */
    private ResJobDTO convertToResJobDTO(Job job) {
        ResJobDTO dto = new ResJobDTO();

        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setLocation(job.getLocation());
        dto.setSalary(job.getSalary());
        dto.setQuantity(job.getQuantity());
        dto.setLevel(job.getLevel());
        dto.setDescription(job.getDescription());
        dto.setStartDate(job.getStartDate());
        dto.setEndDate(job.getEndDate());
        dto.setActive(job.isActive());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setUpdatedAt(job.getUpdatedAt());
        dto.setCreatedBy(job.getCreatedBy());
        dto.setUpdatedBy(job.getUpdatedBy());

        if (job.getSkills() != null) {
            dto.setSkills(job.getSkills().stream()
                    .map(Skill::getName)
                    .collect(Collectors.toList()));
        }

        if (job.getCompanyId() != null) {
            ResJobDTO.CompanyInfo companyInfo = companyFetchService.fetchCompany(job.getCompanyId());
            dto.setCompany(companyInfo);
        }

        return dto;
    }

}

