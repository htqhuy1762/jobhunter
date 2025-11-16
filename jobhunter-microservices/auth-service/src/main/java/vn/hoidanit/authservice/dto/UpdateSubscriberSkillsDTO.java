package vn.hoidanit.authservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubscriberSkillsDTO {

    @NotEmpty(message = "At least one skill is required")
    private List<Long> skillIds;
}

