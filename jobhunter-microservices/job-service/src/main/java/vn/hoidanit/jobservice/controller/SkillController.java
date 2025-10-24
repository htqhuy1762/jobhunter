package vn.hoidanit.jobservice.controller;

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
import vn.hoidanit.jobservice.domain.Skill;
import vn.hoidanit.jobservice.domain.response.RestResponse;
import vn.hoidanit.jobservice.dto.ResultPaginationDTO;
import vn.hoidanit.jobservice.service.SkillService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;

    @PostMapping("/skills")
    public ResponseEntity<RestResponse<Skill>> create(@Valid @RequestBody Skill skill) {
        if (skill.getName() != null && this.skillService.isNameExist(skill.getName())) {
            return ResponseEntity.badRequest().build();
        }

        Skill createdSkill = this.skillService.createSkill(skill);
        return RestResponse.created(createdSkill, "Create skill successfully");
    }

    @PutMapping("/skills")
    public ResponseEntity<RestResponse<Skill>> update(@Valid @RequestBody Skill skill) {
        Skill currentSkill = this.skillService.fetchSkillById(skill.getId());

        if (currentSkill == null) {
            return ResponseEntity.notFound().build();
        }

        if (skill.getName() != null && this.skillService.isNameExist(skill.getName())) {
            return ResponseEntity.badRequest().build();
        }

        currentSkill.setName(skill.getName());
        Skill updatedSkill = this.skillService.updateSkill(currentSkill);
        return RestResponse.ok(updatedSkill, "Update skill successfully");
    }

    @GetMapping("/skills")
    public ResponseEntity<RestResponse<ResultPaginationDTO>> getAll(@Filter Specification<Skill> spec, Pageable pageable)  {
        ResultPaginationDTO result = this.skillService.fetchAllSkills(spec, pageable);
        return RestResponse.ok(result, "Fetch skills successfully");
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<RestResponse<Void>> delete(@PathVariable("id") long id) {
        Skill currSkill = this.skillService.fetchSkillById(id);
        if(currSkill == null) {
            return ResponseEntity.notFound().build();
        }

        this.skillService.deleteSkill(id);
        return RestResponse.ok(null, "Delete skill successfully");
    }
}


