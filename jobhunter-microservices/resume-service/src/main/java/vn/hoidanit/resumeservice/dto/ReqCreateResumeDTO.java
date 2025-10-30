package vn.hoidanit.resumeservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.resumeservice.util.constant.ResumeStateEnum;

@Getter
@Setter
public class ReqCreateResumeDTO {
    @NotBlank(message = "email không được để trống")
    private String email;

    @NotBlank(message = "url không được để trống (upload cv chưa thành công)")
    private String url;

    private ResumeStateEnum status;

    @NotNull(message = "user không được để trống")
    private UserRef user;

    @NotNull(message = "job không được để trống")
    private JobRef job;

    @Getter
    @Setter
    public static class UserRef {
        @NotNull(message = "user.id không được để trống")
        private Long id;
    }

    @Getter
    @Setter
    public static class JobRef {
        @NotNull(message = "job.id không được để trống")
        private Long id;
    }
}

