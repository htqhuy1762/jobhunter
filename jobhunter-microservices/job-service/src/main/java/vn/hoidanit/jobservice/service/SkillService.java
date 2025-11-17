package vn.hoidanit.jobservice.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobservice.domain.Skill;
import vn.hoidanit.jobservice.dto.ResultPaginationDTO;
import vn.hoidanit.jobservice.dto.SkillEvent;
import vn.hoidanit.jobservice.repository.SkillRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {
    private final SkillRepository skillRepository;
    private final KafkaTemplate<String, SkillEvent> kafkaTemplate;

    public boolean isNameExist(String name) {
        return this.skillRepository.existsByName(name);
    }

    public Skill fetchSkillById (long id) {
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        if(skillOptional.isPresent()) {
            return skillOptional.get();
        }
        return null;
    }

    public Skill createSkill (Skill s) {
        Skill savedSkill = this.skillRepository.save(s);

        // Publish event to Kafka for other services to sync
        publishSkillEvent("SKILL_CREATED", savedSkill);

        return savedSkill;
    }

    public Skill updateSkill(Skill s) {
        Skill updatedSkill = this.skillRepository.save(s);

        // Publish event to Kafka for other services to sync
        publishSkillEvent("SKILL_UPDATED", updatedSkill);

        return updatedSkill;
    }

    public void deleteSkill(long id) {
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        Skill currentSkill = skillOptional.get();
        // In microservices, we only remove from jobs in this service
        currentSkill.getJobs().forEach(job -> job.getSkills().remove(currentSkill));

        this.skillRepository.delete(currentSkill);

        // Publish event to Kafka for other services to sync
        publishSkillEvent("SKILL_DELETED", currentSkill);
    }

    public ResultPaginationDTO fetchAllSkills(Specification<Skill> spec, Pageable pageable) {
        Page<Skill> pageSkill = this.skillRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageSkill.getTotalPages());
        mt.setTotal(pageSkill.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(pageSkill.getContent());

        return rs;
    }

    /**
     * Publish skill event to Kafka for other services to sync
     * Topic: skill-events
     */
    private void publishSkillEvent(String eventType, Skill skill) {
        try {
            SkillEvent event = SkillEvent.builder()
                .eventType(eventType)
                .skillId(skill.getId())
                .name(skill.getName())
                .timestamp(Instant.now())
                .source("job-service")
                .build();

            kafkaTemplate.send("skill-events", String.valueOf(skill.getId()), event);

            log.info("Published {} event for skill: {} (ID: {})",
                     eventType, skill.getName(), skill.getId());
        } catch (Exception e) {
            log.error("Failed to publish skill event: {}", eventType, e);
            // Don't throw - skill operation should succeed even if event fails
        }
    }
}


