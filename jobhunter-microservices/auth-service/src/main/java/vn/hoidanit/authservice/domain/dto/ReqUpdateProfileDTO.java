package vn.hoidanit.authservice.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.authservice.domain.User.GenderEnum;

/**
 * DTO for user self-updating their own profile
 * Only allows updating safe fields (name, age, gender, address)
 * Does NOT allow changing: email, password, role, company
 */
@Getter
@Setter
public class ReqUpdateProfileDTO {

    @NotBlank(message = "Name không được để trống")
    private String name;

    @Min(value = 0, message = "Age phải lớn hơn hoặc bằng 0")
    private int age;

    private GenderEnum gender;

    private String address;
}

