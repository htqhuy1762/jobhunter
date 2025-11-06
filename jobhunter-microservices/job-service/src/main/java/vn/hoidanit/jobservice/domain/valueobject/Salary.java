package vn.hoidanit.jobservice.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Value Object representing salary in DDD
 * Immutable and compared by value, not identity
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Salary {

    private double amount;
    private String currency;

    public Salary(double amount, String currency) {
        validateAmount(amount);
        validateCurrency(currency);
        this.amount = amount;
        this.currency = currency;
    }

    public Salary(double amount) {
        this(amount, "VND");
    }

    private void validateAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Salary amount must be positive");
        }
        if (amount > 1_000_000_000) {
            throw new IllegalArgumentException("Salary amount exceeds maximum limit");
        }
    }

    private void validateCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency is required");
        }
    }

    public boolean isHigherThan(Salary other) {
        // Simplified: assume same currency for comparison
        return this.amount > other.amount;
    }

    public Salary increase(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        double newAmount = this.amount * (1 + percentage / 100);
        return new Salary(newAmount, this.currency);
    }

    @Override
    public String toString() {
        return String.format("%.2f %s", amount, currency);
    }
}