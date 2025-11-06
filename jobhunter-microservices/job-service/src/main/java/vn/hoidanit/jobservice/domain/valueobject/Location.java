package vn.hoidanit.jobservice.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value Object representing job location in DDD
 * Immutable and compared by value
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    private String city;
    private String country;

    public Location(String city, String country) {
        validateCity(city);
        validateCountry(country);
        this.city = city;
        this.country = country;
    }

    public Location(String fullLocation) {
        // Parse "Ho Chi Minh, Vietnam" format
        if (fullLocation == null || fullLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }

        String[] parts = fullLocation.split(",");
        if (parts.length >= 2) {
            this.city = parts[0].trim();
            this.country = parts[1].trim();
        } else {
            this.city = fullLocation.trim();
            this.country = "Vietnam";
        }

        validateCity(this.city);
        validateCountry(this.country);
    }

    private void validateCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
    }

    private void validateCountry(String country) {
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Country is required");
        }
    }

    public String getFullLocation() {
        return city + ", " + country;
    }

    public boolean isInCountry(String countryName) {
        return this.country.equalsIgnoreCase(countryName);
    }

    @Override
    public String toString() {
        return getFullLocation();
    }
}

