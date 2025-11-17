package vn.hoidanit.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriberDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    @NotEmpty(message = "At least one skill is required")
    @JsonProperty("skills")
    private List<SkillDTO> skills;

    /**
     * Get skill IDs from skills list
     * Supports both String and Number ID formats
     */
    public List<Long> getSkillIds() {
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }
        return skills.stream()
                .map(SkillDTO::getIdAsLong)
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    /**
     * Inner DTO for skill with flexible ID type
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillDTO {
        private Object id; // Accept both String and Long

        /**
         * Get ID as Long, converting from String if needed
         */
        public Long getIdAsLong() {
            if (id == null) {
                return null;
            }
            if (id instanceof String) {
                try {
                    return Long.parseLong((String) id);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (id instanceof Number) {
                return ((Number) id).longValue();
            }
            return null;
        }
    }
}