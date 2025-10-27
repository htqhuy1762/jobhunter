package vn.hoidanit.jobservice.client;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobservice.dto.CompanyDTO;
import vn.hoidanit.jobservice.dto.RestResponseWrapper;

@Component
@Slf4j
public class CompanyClientFallback implements CompanyClient {

    @Override
    public RestResponseWrapper<CompanyDTO> getCompanyById(Long id) {
        log.error("Fallback triggered for getCompanyById with id: {}", id);

        // Return a default company object when service is unavailable
        CompanyDTO fallbackCompany = new CompanyDTO();
        fallbackCompany.setId(id);
        fallbackCompany.setName("Company information unavailable");
        fallbackCompany.setDescription("Unable to fetch company details at this time");
        fallbackCompany.setLogo(null);

        RestResponseWrapper<CompanyDTO> response = new RestResponseWrapper<>();
        response.setStatusCode(200);
        response.setMessage("Fallback response");
        response.setData(fallbackCompany);

        return response;
    }
}

