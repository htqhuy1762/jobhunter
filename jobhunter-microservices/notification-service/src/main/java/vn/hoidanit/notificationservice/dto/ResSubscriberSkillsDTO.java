package vn.hoidanit.notificationservice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResSubscriberSkillsDTO {
    private List<SkillInfo> skills;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfo {
        private long id;
        private String name;
    }
}

