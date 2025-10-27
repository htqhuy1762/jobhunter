package vn.hoidanit.jobservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import vn.hoidanit.jobservice.dto.CompanyDTO;
import vn.hoidanit.jobservice.dto.RestResponseWrapper;

@FeignClient(name = "company-service", fallback = CompanyClientFallback.class)
public interface CompanyClient {

    @GetMapping("/api/v1/companies/{id}")
    RestResponseWrapper<CompanyDTO> getCompanyById(@PathVariable("id") Long id);
}