package vn.hoidanit.notificationservice.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateSubscriberDTO {

    @NotBlank(message = "email không được để trống")
    private String email;

    @NotBlank(message = "name không được để trống")
    private String name;

    @NotNull(message = "skills không được để trống")
    private List<SkillRef> skills;

    @Getter
    @Setter
    public static class SkillRef {
        @NotNull(message = "skill.id không được để trống")
        private Long id;
    }
}

