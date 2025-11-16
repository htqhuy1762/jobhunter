package vn.hoidanit.jobservice.util.constant;

/**
 * Location Enum for standardized city names
 * Used to ensure consistent location data across the system
 * Matches frontend definition exactly
 */
public enum LocationEnum {
    HANOI,
    HOCHIMINH,
    DANANG,
    OTHER;

    /**
     * Get display name for frontend
     */
    public String getDisplayName() {
        return switch (this) {
            case HANOI -> "Hà Nội";
            case HOCHIMINH -> "Hồ Chí Minh";
            case DANANG -> "Đà Nẵng";
            case OTHER -> "Others";
        };
    }

    /**
     * Parse from string (case-insensitive)
     * Handles various input formats
     */
    public static LocationEnum fromString(String location) {
        if (location == null || location.trim().isEmpty()) {
            return OTHER;
        }

        String normalized = location.toUpperCase()
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "")
                .replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A")
                .replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E")
                .replaceAll("[ÌÍỊỈĨ]", "I")
                .replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O")
                .replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U")
                .replaceAll("[ỲÝỴỶỸ]", "Y")
                .replaceAll("[Đ]", "D");

        // Match patterns
        if (normalized.contains("HANOI") || normalized.contains("HN")) {
            return HANOI;
        } else if (normalized.contains("HOCHIMINH") || normalized.contains("HCM") ||
                   normalized.contains("SAIGON") || normalized.contains("SGN")) {
            return HOCHIMINH;
        } else if (normalized.contains("DANANG") || normalized.contains("DN")) {
            return DANANG;
        }

        // Try exact enum match
        try {
            return LocationEnum.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}

