package vn.hoidanit.resumeservice.client;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.resumeservice.dto.JobDTO;

@Component
@Slf4j
public class JobClientFallback implements JobClient {

    @Override
    public JobDTO getJobById(Long id) {
        log.error("Fallback triggered for getJobById with id: {}", id);

        JobDTO fallbackJob = new JobDTO();
        fallbackJob.setId(id);
        fallbackJob.setName("Job information unavailable");

        return fallbackJob;
    }
}