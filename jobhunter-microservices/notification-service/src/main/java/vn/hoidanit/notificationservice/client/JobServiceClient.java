package vn.hoidanit.notificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "job-service")
public interface JobServiceClient {

    @GetMapping("/api/v1/skills/{id}")
    SkillResponse getSkillById(@PathVariable("id") Long id);

    // DTO to match job-service response structure
    public static class SkillResponse {
        private int statusCode;
        private SkillData data;
        private String message;

        public SkillData getData() {
            return data;
        }

        public void setData(SkillData data) {
            this.data = data;
        }
    }

    public static class SkillData {
        private long id;
        private String name;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

