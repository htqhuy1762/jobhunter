package vn.hoidanit.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event DTO for skill synchronization from Job Service
 * Published by Job Service when skills are created/updated/deleted
 * Consumed by Auth Service to maintain local skills cache
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillEvent {

    /**
     * Event type: SKILL_CREATED, SKILL_UPDATED, SKILL_DELETED
     */
    private String eventType;

    /**
     * Skill ID (primary key)
     */
    private Long skillId;

    /**
     * Skill name
     */
    private String name;

    /**
     * Event timestamp
     */
    private Instant timestamp;

    /**
     * Source service
     */
    private String source;
}

