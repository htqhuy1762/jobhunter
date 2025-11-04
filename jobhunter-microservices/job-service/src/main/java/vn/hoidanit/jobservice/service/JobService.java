package vn.hoidanit.jobservice.service;

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
import vn.hoidanit.jobservice.repository.JobRepository;
import vn.hoidanit.jobservice.repository.SkillRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final CompanyFetchService companyFetchService;

    public ResCreateJobDTO create(Job j) {
        // Check Skill
        if (j.getSkills() != null) {
            List<Long> reqSkills = j.getSkills().stream().map(x -> x.getId()).collect(Collectors.toList());
            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            j.setSkills(dbSkills);
        }

        // In microservices, companyId is already set, no need to fetch Company entity
        // The relationship is handled via service communication

        //create job
        Job currentJob = this.jobRepository.save(j);

        //convert to response
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

    public ResUpdateJobDTO update(Job j, Job jobInDB) {
        // check skills
        if(j.getSkills() != null) {
            List<Long> reqSkills = j.getSkills().stream().map(x -> x.getId()).collect(Collectors.toList());
            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            jobInDB.setSkills(dbSkills);
        }

        // update correct info
        jobInDB.setName(j.getName());
        jobInDB.setLocation(j.getLocation());
        jobInDB.setSalary(j.getSalary());
        jobInDB.setQuantity(j.getQuantity());
        jobInDB.setLevel(j.getLevel());
        jobInDB.setStartDate(j.getStartDate());
        jobInDB.setEndDate(j.getEndDate());
        jobInDB.setActive(j.isActive());
        if(j.getCompanyId() != null) {
            jobInDB.setCompanyId(j.getCompanyId());
        }

        //update job
        Job currentJob = this.jobRepository.save(jobInDB);

        //convert to response
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

