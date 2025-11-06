package vn.hoidanit.jobservice.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobservice.domain.Job;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Domain Service for Job Validation logic
 * Contains complex validation rules that span multiple concepts
 */
@Service
@RequiredArgsConstructor
public class JobValidationDomainService {

    /**
     * Validate if a job posting period is reasonable
     * Business rule: Job should be posted for at least 7 days and no more than 180 days
     */
    public boolean isValidPostingPeriod(Instant startDate, Instant endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return days >= 7 && days <= 180;
    }

    /**
     * Validate if salary is reasonable for the level
     * Business rule: Different levels should have appropriate salary ranges
     */
    public boolean isSalaryReasonableForLevel(double salary, String level) {
        if (salary <= 0) {
            return false;
        }

        // Simplified rules in VND (millions)
        switch (level.toUpperCase()) {
            case "INTERN":
                return salary >= 3 && salary <= 8;
            case "FRESHER":
                return salary >= 8 && salary <= 15;
            case "JUNIOR":
                return salary >= 12 && salary <= 25;
            case "MIDDLE":
                return salary >= 20 && salary <= 50;
            case "SENIOR":
                return salary >= 40 && salary <= 100;
            case "LEADER":
            case "MANAGER":
                return salary >= 60 && salary <= 150;
            default:
                return true; // Unknown level, skip validation
        }
    }

    /**
     * Check if job quantity is reasonable
     * Business rule: Should hire reasonable number of people (1-100)
     */
    public boolean isReasonableQuantity(int quantity) {
        return quantity >= 1 && quantity <= 100;
    }

    /**
     * Validate if job can be closed
     * Business rule: Job can only be closed if it's currently active
     */
    public boolean canBeClosed(Job job) {
        return job.isActive();
    }

    /**
     * Validate if job can be extended
     * Business rule: Can only extend jobs that haven't ended yet or ended within last 7 days
     */
    public boolean canBeExtended(Job job) {
        Instant now = Instant.now();
        Instant endDate = job.getEndDate();

        if (endDate.isAfter(now)) {
            return true; // Not ended yet
        }

        // Check if ended within last 7 days
        long daysSinceEnd = ChronoUnit.DAYS.between(endDate, now);
        return daysSinceEnd <= 7;
    }

    /**
     * Calculate recommended posting period in days based on job level
     * Higher positions need longer posting periods
     */
    public int getRecommendedPostingDays(String level) {
        switch (level.toUpperCase()) {
            case "INTERN":
            case "FRESHER":
                return 14;
            case "JUNIOR":
            case "MIDDLE":
                return 21;
            case "SENIOR":
                return 30;
            case "LEADER":
            case "MANAGER":
                return 45;
            default:
                return 21;
        }
    }
}


