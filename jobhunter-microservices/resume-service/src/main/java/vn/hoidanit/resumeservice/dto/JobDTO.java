package vn.hoidanit.resumeservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobDTO {
    private Long id;
    private String name;
    private String location;
    private double salary;
    private CompanyDTO company;
}

