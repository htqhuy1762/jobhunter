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
import vn.hoidanit.resumeservice.domain.Resume;
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
    public ResponseEntity<ResCreateResumeDTO> create(@Valid @RequestBody Resume resume) {
        // Check id exists
        boolean isIdExist = this.resumeService.checkResumeExistByUserAndJob(resume);
        if(!isIdExist) {
            return ResponseEntity.badRequest().build();
        }

        // Create resume
        return ResponseEntity.status(HttpStatus.CREATED).body(this.resumeService.create(resume));
    }

    @PutMapping("/resumes")
    public ResponseEntity<ResUpdateResumeDTO> update(@RequestBody Resume resume) {
        // check id exists
        Optional<Resume> resumeOptional = this.resumeService.fetchById(resume.getId());
        if(resumeOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Resume reqResume = resumeOptional.get();
        reqResume.setStatus(resume.getStatus());

        return ResponseEntity.ok().body(this.resumeService.update(reqResume));
    }

    @DeleteMapping("/resumes/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        Optional<Resume> resumeOptional = this.resumeService.fetchById(id);
        if(resumeOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        this.resumeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resumes/{id}")
    public ResponseEntity<ResFetchResumeDTO> fetchById(@PathVariable("id") long id) {
        Optional<Resume> resumeOptional = this.resumeService.fetchById(id);
        if(resumeOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(this.resumeService.getResume(resumeOptional.get()));
    }

    @GetMapping("/resumes")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<Resume> spec,
            Pageable pageable) {
        return ResponseEntity.ok().body(this.resumeService.fetchAllResume(spec, pageable));
    }

    @GetMapping("/resumes/by-user")
    public ResponseEntity<ResultPaginationDTO> fetchByUser(Pageable pageable) {
        return ResponseEntity.ok().body(this.resumeService.fetchAllResumeByUser(pageable));
    }
}


