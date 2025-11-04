package vn.hoidanit.jobservice.service;

import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobservice.client.CompanyClient;
import vn.hoidanit.jobservice.dto.CompanyDTO;
import vn.hoidanit.jobservice.dto.ResJobDTO;

/**
 * Service dedicated to fetching company information with Circuit Breaker protection.
 *
 * This is a separate service to:
 * 1. Follow Single Responsibility Principle
 * 2. Enable Circuit Breaker to work properly (external calls go through proxy)
 * 3. Make the code more testable and reusable
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyFetchService {

    private final CompanyClient companyClient;

    /**
     * Fetch company information with Circuit Breaker and Retry protection.
     *
     * This method is called from OTHER services (e.g., JobService),
     * so Spring AOP proxy can intercept it properly.
     *
     * @param companyId The company ID to fetch
     * @return CompanyInfo with fallback if service is down
     */
    @CircuitBreaker(name = "companyService", fallbackMethod = "fetchCompanyFallback")
    @Retry(name = "companyService")
    public ResJobDTO.CompanyInfo fetchCompany(Long companyId) {
        log.debug("Fetching company with id: {}", companyId);

        var companyResponse = companyClient.getCompanyById(companyId);

        if (companyResponse != null && companyResponse.getData() != null) {
            CompanyDTO company = companyResponse.getData();
            ResJobDTO.CompanyInfo companyInfo = new ResJobDTO.CompanyInfo();
            companyInfo.setId(company.getId());
            companyInfo.setName(company.getName());
            companyInfo.setLogo(company.getLogo());
            return companyInfo;
        }

        return createFallbackCompanyInfo(companyId);
    }

    /**
     * Fallback method when Circuit Breaker is open or service fails.
     * Returns minimal company information.
     */
    public ResJobDTO.CompanyInfo fetchCompanyFallback(Long companyId, Throwable ex) {
        log.warn("Circuit breaker fallback triggered for company {}: {}", companyId, ex.getMessage());
        log.debug("Exception type: {}", ex.getClass().getName());
        return createFallbackCompanyInfo(companyId);
    }

    /**
     * Create minimal company info for fallback scenarios.
     */
    private ResJobDTO.CompanyInfo createFallbackCompanyInfo(Long companyId) {
        ResJobDTO.CompanyInfo companyInfo = new ResJobDTO.CompanyInfo();
        companyInfo.setId(companyId);
        companyInfo.setName("Company information unavailable");
        companyInfo.setLogo(null);
        return companyInfo;
    }
}

