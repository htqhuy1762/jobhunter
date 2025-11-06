package vn.hoidanit.jobservice.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobservice.domain.Job;
import vn.hoidanit.jobservice.domain.Skill;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Domain Service for Job Matching logic
 * Domain Services contain business logic that doesn't naturally belong to any single entity
 * This is pure domain logic without infrastructure concerns
 */
@Service
@RequiredArgsConstructor
public class JobMatchingDomainService {

    /**
     * Calculate match score between a set of candidate skills and job requirements
     * @param candidateSkills Skills that the candidate has
     * @param job The job to match against
     * @return Match score from 0 to 100
     */
    public double calculateMatchScore(Set<Long> candidateSkills, Job job) {
        if (candidateSkills == null || candidateSkills.isEmpty()) {
            return 0.0;
        }

        List<Skill> requiredSkills = job.getRequiredSkills();
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return 0.0;
        }

        Set<Long> requiredSkillIds = requiredSkills.stream()
                .map(Skill::getId)
                .collect(Collectors.toSet());

        // Count matching skills
        long matchingSkills = candidateSkills.stream()
                .filter(requiredSkillIds::contains)
                .count();

        // Calculate percentage
        return (matchingSkills * 100.0) / requiredSkillIds.size();
    }

    /**
     * Check if a candidate is qualified for a job
     * Business rule: candidate must have at least 60% of required skills
     */
    public boolean isQualified(Set<Long> candidateSkills, Job job) {
        double matchScore = calculateMatchScore(candidateSkills, job);
        return matchScore >= 60.0;
    }

    /**
     * Determine match level based on score
     */
    public MatchLevel determineMatchLevel(Set<Long> candidateSkills, Job job) {
        double score = calculateMatchScore(candidateSkills, job);

        if (score >= 90) {
            return MatchLevel.EXCELLENT;
        } else if (score >= 75) {
            return MatchLevel.GOOD;
        } else if (score >= 60) {
            return MatchLevel.FAIR;
        } else if (score >= 40) {
            return MatchLevel.PARTIAL;
        } else {
            return MatchLevel.POOR;
        }
    }

    /**
     * Get missing skills for a candidate to fully qualify for a job
     */
    public List<Skill> getMissingSkills(Set<Long> candidateSkills, Job job) {
        if (candidateSkills == null) {
            return job.getRequiredSkills();
        }

        return job.getRequiredSkills().stream()
                .filter(skill -> !candidateSkills.contains(skill.getId()))
                .collect(Collectors.toList());
    }

    public enum MatchLevel {
        EXCELLENT,  // 90-100%
        GOOD,       // 75-89%
        FAIR,       // 60-74%
        PARTIAL,    // 40-59%
        POOR        // 0-39%
    }
}