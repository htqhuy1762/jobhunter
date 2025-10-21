package vn.hoidanit.resumeservice.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.resumeservice.client.JobClient;
import vn.hoidanit.resumeservice.client.UserClient;
import vn.hoidanit.resumeservice.domain.Resume;
import vn.hoidanit.resumeservice.dto.JobDTO;
import vn.hoidanit.resumeservice.dto.ResCreateResumeDTO;
import vn.hoidanit.resumeservice.dto.ResFetchResumeDTO;
import vn.hoidanit.resumeservice.dto.ResUpdateResumeDTO;
import vn.hoidanit.resumeservice.dto.ResultPaginationDTO;
import vn.hoidanit.resumeservice.dto.UserDTO;
import vn.hoidanit.resumeservice.repository.ResumeRepository;
import vn.hoidanit.resumeservice.util.SecurityUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {
    private final ResumeRepository resumeRepository;
        // Validate userId and jobId are not null

    public Optional<Resume> fetchById(long id) {
        return this.resumeRepository.findById(id);

        // Call other services to validate if user and job exist
        try {
            UserDTO user = userClient.getUserById(resume.getUserId());
            if (user == null) {
                log.error("User with id {} not found", resume.getUserId());
                return false;
            }

            JobDTO job = jobClient.getJobById(resume.getJobId());
            if (job == null) {
                log.error("Job with id {} not found", resume.getJobId());
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Error calling other services: {}", e.getMessage());
            return false;
        }

    public boolean checkResumeExistByUserAndJob(Resume resume) {
        // In microservices, we only check if userId and jobId are not null
        // The actual validation should be done via inter-service communication
        if (resume.getUserId() == null || resume.getJobId() == null) {
            return false;
        }
        return true;
    }

    public ResCreateResumeDTO create(Resume resume) {
        resume = this.resumeRepository.save(resume);

        ResCreateResumeDTO res = new ResCreateResumeDTO();
        res.setId(resume.getId());
        res.setCreatedAt(resume.getCreatedAt());
        res.setCreatedBy(resume.getCreatedBy());

        // Fetch user info via FeignClient

            try {
                UserDTO user = userClient.getUserById(resume.getUserId());
                if (user != null) {
                    res.setUser(new ResFetchResumeDTO.UserResume(user.getId(), user.getName()));
                }
            } catch (Exception e) {
                log.error("Error fetching user info: {}", e.getMessage());
                res.setUser(new ResFetchResumeDTO.UserResume(resume.getUserId(), "User #" + resume.getUserId()));
            }
        resume = this.resumeRepository.save(resume);

        // Fetch job info via FeignClient
        ResUpdateResumeDTO res = new ResUpdateResumeDTO();
            try {
                JobDTO job = jobClient.getJobById(resume.getJobId());
                if (job != null) {
                    res.setJob(new ResFetchResumeDTO.JobResume(job.getId(), job.getName()));
                    // Set company name if available
                    if (job.getCompany() != null) {
                        res.setCompanyName(job.getCompany().getName());
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching job info: {}", e.getMessage());
                res.setJob(new ResFetchResumeDTO.JobResume(resume.getJobId(), "Job #" + resume.getJobId()));
            }
        res.setUpdatedBy(resume.getUpdatedBy());
        return res;
    }

    public void delete(long id) {
        this.resumeRepository.deleteById(id);
    }

    public ResFetchResumeDTO getResume(Resume resume) {
        ResFetchResumeDTO res = new ResFetchResumeDTO();
        res.setId(resume.getId());
        res.setEmail(resume.getEmail());
        res.setUrl(resume.getUrl());
        res.setStatus(resume.getStatus());
        res.setCreatedAt(resume.getCreatedAt());
        res.setUpdatedAt(resume.getUpdatedAt());
        res.setCreatedBy(resume.getCreatedBy());
        res.setUpdatedBy(resume.getUpdatedBy());

        // In microservices, we need to fetch user and job info via inter-service communication
        // For now, just set basic info with IDs
        if (resume.getUserId() != null) {
            res.setUser(new ResFetchResumeDTO.UserResume(resume.getUserId(), "User #" + resume.getUserId()));
        }
        if (resume.getJobId() != null) {
            res.setJob(new ResFetchResumeDTO.JobResume(resume.getJobId(), "Job #" + resume.getJobId()));
        }

        return res;
    }

    public ResultPaginationDTO fetchAllResume(Specification<Resume> spec, Pageable pageable) {
        Page<Resume> pageUser = this.resumeRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());

        result.setMeta(meta);

        // remove sensitive information
        List<ResFetchResumeDTO> listResume = pageUser.getContent().stream().map(item -> this.getResume(item))
                .collect(Collectors.toList());
        result.setResult(listResume);

        return result;
    }

    public ResultPaginationDTO fetchAllResumeByUser(Pageable pageable) {
        // In microservices, filter by current user email
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";

        // For now, fetch all - should be filtered by email in real implementation
        Page<Resume> pageResume = this.resumeRepository.findAll(pageable);

        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(pageResume.getTotalPages());
        meta.setTotal(pageResume.getTotalElements());

        result.setMeta(meta);

        List<ResFetchResumeDTO> listResume = pageResume.getContent()
                .stream().map(item -> this.getResume(item))
                .collect(Collectors.toList());

        result.setResult(listResume);
        return result;
    }
}

