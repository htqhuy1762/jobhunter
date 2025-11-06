package vn.hoidanit.jobservice.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Value Object representing a date range in DDD
 * Immutable and compared by value
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DateRange {

    private Instant startDate;
    private Instant endDate;

    public DateRange(Instant startDate, Instant endDate) {
        validateDateRange(startDate, endDate);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    private void validateDateRange(Instant startDate, Instant endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date is required");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    public boolean isActive() {
        Instant now = Instant.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(endDate);
    }

    public boolean isUpcoming() {
        return Instant.now().isBefore(startDate);
    }

    public long getDurationInDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public DateRange extend(long daysToExtend) {
        if (daysToExtend <= 0) {
            throw new IllegalArgumentException("Extension days must be positive");
        }
        Instant newEndDate = endDate.plus(daysToExtend, ChronoUnit.DAYS);
        return new DateRange(startDate, newEndDate);
    }

    public boolean overlaps(DateRange other) {
        return !this.endDate.isBefore(other.startDate) &&
               !other.endDate.isBefore(this.startDate);
    }

    @Override
    public String toString() {
        return String.format("%s to %s", startDate, endDate);
    }
}


