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
    public ResponseEntity<RestResponse<Void>> delete(@PathVariable("id") long id) {
        Optional<Resume> resumeOptional = this.resumeService.fetchById(id);
        if(resumeOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        this.resumeService.delete(id);
        return RestResponse.ok(null, "Delete resume successfully");
    }

    @GetMapping("/resumes/{id}")
    public ResponseEntity<RestResponse<ResFetchResumeDTO>> fetchById(@PathVariable("id") long id) {
        Optional<Resume> resumeOptional = this.resumeService.fetchById(id);
        if(resumeOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ResFetchResumeDTO resumeDTO = this.resumeService.getResume(resumeOptional.get());
        return RestResponse.ok(resumeDTO, "Fetch resume by id successfully");
    }

    @GetMapping("/resumes")
    public ResponseEntity<RestResponse<ResultPaginationDTO>> fetchAll(
            @Filter Specification<Resume> spec,
            @PageableDefault(page = 1, size = 10, sort = "id", direction = "desc") Pageable pageable) {

        ResultPaginationDTO result = this.resumeService.fetchAllResume(spec, pageable);
        return RestResponse.ok(result, "Fetch resumes successfully");
    }

    @GetMapping("/resumes/by-user")
    public ResponseEntity<RestResponse<ResultPaginationDTO>> fetchByUser(
            @PageableDefault(page = 1, size = 10, sort = "id", direction = "desc") Pageable pageable) {

        ResultPaginationDTO result = this.resumeService.fetchAllResumeByUser(pageable);
        return RestResponse.ok(result, "Fetch resumes by user successfully");
    }
}

