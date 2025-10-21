package vn.hoidanit.jobservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyDTO {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String logo;
}

