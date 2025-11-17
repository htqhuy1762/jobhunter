package vn.hoidanit.authservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.hoidanit.authservice.domain.Skill;
import vn.hoidanit.authservice.dto.SkillEvent;
import vn.hoidanit.authservice.repository.SkillRepository;

/**
 * Kafka consumer for skill synchronization
 * Listens to skill events from Job Service and maintains local skills cache
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SkillEventConsumer {

    private final SkillRepository skillRepository;

    /**
     * Handle skill events from Job Service
     * Events: SKILL_CREATED, SKILL_UPDATED, SKILL_DELETED
     */
    @KafkaListener(
        topics = "skill-events",
        groupId = "auth-service-skill-sync",
        containerFactory = "skillEventKafkaListenerContainerFactory"
    )
    public void handleSkillEvent(SkillEvent event) {
        log.info("Received skill event: {} for skill ID: {} ({})",
                 event.getEventType(), event.getSkillId(), event.getName());

        try {
            switch (event.getEventType()) {
                case "SKILL_CREATED":
                case "SKILL_UPDATED":
                    syncSkill(event);
                    break;

                case "SKILL_DELETED":
                    deleteSkill(event.getSkillId());
                    break;

                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing skill event: {}", event, e);
            // In production: implement retry mechanism or dead letter queue
        }
    }

    /**
     * Sync skill to local cache (upsert operation)
     */
    private void syncSkill(SkillEvent event) {
        Skill skill = skillRepository.findById(event.getSkillId())
            .orElse(new Skill());

        skill.setId(event.getSkillId());
        skill.setName(event.getName());
        skill.setCreatedBy("sync-from-job-service");

        skillRepository.save(skill);

        log.info("Synced skill: {} (ID: {})", event.getName(), event.getSkillId());
    }

    /**
     * Delete skill from local cache
     */
    private void deleteSkill(Long skillId) {
        if (skillRepository.existsById(skillId)) {
            skillRepository.deleteById(skillId);
            log.info("Deleted skill ID: {}", skillId);
        } else {
            log.warn("Skill ID {} not found in local cache, skip deletion", skillId);
        }
    }
}

