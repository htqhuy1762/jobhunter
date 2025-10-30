package vn.hoidanit.authservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqChangePasswordDTO {

    @NotBlank(message = "Old password không được để trống")
    private String oldPassword;

    @NotBlank(message = "New password không được để trống")
    private String newPassword;

    @NotBlank(message = "Confirm password không được để trống")
    private String confirmPassword;
}

