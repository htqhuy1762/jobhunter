package vn.hoidanit.resumeservice.controller;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.resumeservice.annotation.PageableDefault;
import vn.hoidanit.resumeservice.annotation.RequireRole;
import vn.hoidanit.resumeservice.domain.Resume;
import vn.hoidanit.resumeservice.domain.response.RestResponse;
import vn.hoidanit.resumeservice.dto.ResCreateResumeDTO;
import vn.hoidanit.resumeservice.dto.ResFetchResumeDTO;
import vn.hoidanit.resumeservice.dto.ResUpdateResumeDTO;
import vn.hoidanit.resumeservice.dto.ResultPaginationDTO;
import vn.hoidanit.resumeservice.service.ResumeService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ResumeController {
    private final ResumeService resumeService;

    @PostMapping("/resumes")
    @RequireRole({"ROLE_USER", "ROLE_HR", "ROLE_ADMIN"})
    public ResponseEntity<RestResponse<ResCreateResumeDTO>> create(@Valid @RequestBody Resume resume) {
        // Check id exists
        boolean isIdExist = this.resumeService.checkResumeExistByUserAndJob(resume);
        if(!isIdExist) {
            return ResponseEntity.badRequest().build();
        }

        // Create resume
        ResCreateResumeDTO createdResume = this.resumeService.create(resume);
        return RestResponse.created(createdResume, "Create resume successfully");
    }

    @PutMapping("/resumes")
    @RequireRole({"ROLE_HR", "ROLE_ADMIN"})
    public ResponseEntity<RestResponse<ResUpdateResumeDTO>> update(@RequestBody Resume resume) {
        // check id exists
        Optional<Resume> resumeOptional = this.resumeService.fetchById(resume.getId());
        if(resumeOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Resume reqResume = resumeOptional.get();
        reqResume.setStatus(resume.getStatus());

        ResUpdateResumeDTO updatedResume = this.resumeService.update(reqResume);
        return RestResponse.ok(updatedResume, "Update resume successfully");
    }

    @DeleteMapping("/resumes/{id}")
    @RequireRole({"ROLE_USER", "ROLE_HR", "ROLE_ADMIN"})
    public ResponseEntity<RestResponse<Void>> delete(@PathVariable("id") long id) {
        Optional<Resume> resumeOptional = this.resumeService.fetchById(id);
        if(resumeOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Resume resume = resumeOptional.get();

        // Data-level authorization: USER can only delete their own resumes
        Long currentUserId = vn.hoidanit.resumeservice.util.SecurityUtil.getCurrentUserId();
        String currentUserRoles = vn.hoidanit.resumeservice.util.SecurityUtil.getCurrentUserRoles();

        boolean isHrOrAdmin = currentUserRoles != null &&
            (currentUserRoles.contains("ROLE_HR") || currentUserRoles.contains("ROLE_ADMIN"));

        boolean isOwner = currentUserId != null && currentUserId.equals(resume.getUserId());

        if (!isHrOrAdmin && !isOwner) {
            return RestResponse.error(org.springframework.http.HttpStatus.FORBIDDEN,
                "You don't have permission to delete this resume");
        }

        this.resumeService.delete(id);
        return RestResponse.ok(null, "Delete resume successfully");
    }

    @GetMapping("/resumes/{id}")
    @RequireRole({"ROLE_USER", "ROLE_HR", "ROLE_ADMIN"})
    public ResponseEntity<RestResponse<ResFetchResumeDTO>> fetchById(@PathVariable("id") long id) {
        Optional<Resume> resumeOptional = this.resumeService.fetchById(id);
        if(resumeOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Resume resume = resumeOptional.get();

        // Data-level authorization: USER can only view their own resumes
        Long currentUserId = vn.hoidanit.resumeservice.util.SecurityUtil.getCurrentUserId();
        String currentUserRoles = vn.hoidanit.resumeservice.util.SecurityUtil.getCurrentUserRoles();

        boolean isHrOrAdmin = currentUserRoles != null &&
            (currentUserRoles.contains("ROLE_HR") || currentUserRoles.contains("ROLE_ADMIN"));

        boolean isOwner = currentUserId != null && currentUserId.equals(resume.getUserId());

        if (!isHrOrAdmin && !isOwner) {
            return RestResponse.error(org.springframework.http.HttpStatus.FORBIDDEN,
                "You don't have permission to view this resume");
        }

        ResFetchResumeDTO resumeDTO = this.resumeService.getResume(resume);
        return RestResponse.ok(resumeDTO, "Fetch resume by id successfully");
    }

    @GetMapping("/resumes")
    @RequireRole({"ROLE_HR", "ROLE_ADMIN"})
    public ResponseEntity<RestResponse<ResultPaginationDTO>> fetchAll(
            @Filter Specification<Resume> spec,
            @PageableDefault(page = 1, size = 10, sort = "id", direction = "desc") Pageable pageable) {

        ResultPaginationDTO result = this.resumeService.fetchAllResume(spec, pageable);
        return RestResponse.ok(result, "Fetch resumes successfully");
    }

    @GetMapping("/resumes/by-user")
    @RequireRole({"ROLE_USER", "ROLE_HR", "ROLE_ADMIN"})
    public ResponseEntity<RestResponse<ResultPaginationDTO>> fetchByUser(
            @PageableDefault(page = 1, size = 10, sort = "id", direction = "desc") Pageable pageable) {

        ResultPaginationDTO result = this.resumeService.fetchAllResumeByUser(pageable);
        return RestResponse.ok(result, "Fetch resumes by user successfully");
    }
}

