package vn.hoidanit.jobservice.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobservice.domain.Job;
import vn.hoidanit.jobservice.domain.Skill;
import vn.hoidanit.jobservice.dto.ResCreateJobDTO;
import vn.hoidanit.jobservice.dto.ResJobDTO;
import vn.hoidanit.jobservice.dto.ResUpdateJobDTO;
import vn.hoidanit.jobservice.dto.ResultPaginationDTO;
import vn.hoidanit.jobservice.kafka.producer.JobEventProducer;
import vn.hoidanit.jobservice.repository.JobRepository;
import vn.hoidanit.jobservice.repository.SkillRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final CompanyFetchService companyFetchService;
    private final JobEventProducer jobEventProducer;

    public ResCreateJobDTO create(Job job) {
        attachSkillsToJob(job);
        Job savedJob = jobRepository.save(job);

        // Publish Kafka event for job alerts
        jobEventProducer.publishJobCreated(savedJob);

        return mapToCreateDTO(savedJob);
    }

    public Optional<Job> fetchJobById(long id) {
        return jobRepository.findById(id);
    }

    public ResJobDTO fetchJobByIdWithCompany(long id) {
        return jobRepository.findById(id)
                .map(this::convertToResJobDTO)
                .orElse(null);
    }

    public ResUpdateJobDTO update(Job job, Job existingJob) {
        attachSkillsToJob(job);
        updateJobFields(existingJob, job);
        Job savedJob = jobRepository.save(existingJob);
        return mapToUpdateDTO(savedJob);
    }

    public void delete(long id) {
        jobRepository.deleteById(id);
    }

    public ResultPaginationDTO fetchAll(Specification<Job> spec, Pageable pageable) {
        Page<Job> jobPage = jobRepository.findAll(spec, pageable);

        List<ResJobDTO> jobDTOs = jobPage.getContent().stream()
                .map(this::convertToResJobDTO)
                .collect(Collectors.toList());

        return buildPaginationResult(jobPage, jobDTOs, pageable);
    }

    private void attachSkillsToJob(Job job) {
        if (job.getSkills() == null || job.getSkills().isEmpty()) {
            return;
        }

        List<Long> skillIds = job.getSkills().stream()
                .map(Skill::getId)
                .collect(Collectors.toList());

        List<Skill> validSkills = skillRepository.findByIdIn(skillIds);
        job.setSkills(validSkills);
    }

    private void updateJobFields(Job target, Job source) {
        target.setName(source.getName());
        target.setLocation(source.getLocation());
        target.setSalary(source.getSalary());
        target.setQuantity(source.getQuantity());
        target.setLevel(source.getLevel());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        target.setActive(source.isActive());

        if (source.getCompanyId() != null) {
            target.setCompanyId(source.getCompanyId());
        }

        if (source.getSkills() != null) {
            target.setSkills(source.getSkills());
        }
    }

    private ResCreateJobDTO mapToCreateDTO(Job job) {
        ResCreateJobDTO dto = new ResCreateJobDTO();
        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setLocation(job.getLocation());
        dto.setSalary(job.getSalary());
        dto.setQuantity(job.getQuantity());
        dto.setLevel(job.getLevel());
        dto.setStartDate(job.getStartDate());
        dto.setEndDate(job.getEndDate());
        dto.setActive(job.isActive());
        dto.setCreatedBy(job.getCreatedBy());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setSkills(extractSkillNames(job));
        return dto;
    }

    private ResUpdateJobDTO mapToUpdateDTO(Job job) {
        ResUpdateJobDTO dto = new ResUpdateJobDTO();
        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setLocation(job.getLocation());
        dto.setSalary(job.getSalary());
        dto.setQuantity(job.getQuantity());
        dto.setLevel(job.getLevel());
        dto.setStartDate(job.getStartDate());
        dto.setEndDate(job.getEndDate());
        dto.setActive(job.isActive());
        dto.setUpdatedBy(job.getUpdatedBy());
        dto.setUpdatedAt(job.getUpdatedAt());
        dto.setSkills(extractSkillNames(job));
        return dto;
    }

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
        dto.setSkills(extractSkillInfos(job));  // Changed to return objects

        if (job.getCompanyId() != null) {
            ResJobDTO.CompanyInfo companyInfo = companyFetchService.fetchCompany(job.getCompanyId());
            dto.setCompany(companyInfo);
        }

        return dto;
    }

    private List<ResJobDTO.SkillInfo> extractSkillInfos(Job job) {
        if (job.getSkills() == null) {
            return Collections.emptyList();
        }
        return job.getSkills().stream()
                .map(skill -> new ResJobDTO.SkillInfo(skill.getId(), skill.getName()))
                .collect(Collectors.toList());
    }

    private List<String> extractSkillNames(Job job) {
        if (job.getSkills() == null) {
            return Collections.emptyList();
        }
        return job.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toList());
    }

    private ResultPaginationDTO buildPaginationResult(Page<?> page, List<?> content, Pageable pageable) {
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(content);
        return result;
    }
}

