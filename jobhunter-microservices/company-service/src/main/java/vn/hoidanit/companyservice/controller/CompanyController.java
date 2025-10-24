package vn.hoidanit.companyservice.controller;

import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.companyservice.annotation.RequireRole;
import vn.hoidanit.companyservice.domain.Company;
import vn.hoidanit.companyservice.domain.response.RestResponse;
import vn.hoidanit.companyservice.dto.ResultPaginationDTO;
import vn.hoidanit.companyservice.service.CompanyService;
import vn.hoidanit.companyservice.util.SecurityUtil;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {
    private final CompanyService companyService;

    /**
     * Create new company - Only HR and ADMIN can create companies
     */
    @PostMapping
    @RequireRole({"ROLE_HR", "ROLE_ADMIN"})
    public ResponseEntity<RestResponse<Company>> createCompany(@Valid @RequestBody Company reqCompany) {
        log.info("User {} is creating company: {}", SecurityUtil.getCurrentUserInfo(), reqCompany.getName());
        Company company = this.companyService.handleCreateCompany(reqCompany);
        return RestResponse.created(company, "Create company successfully");
    }

    /**
     * Get all companies - Public endpoint (no authentication required via Gateway)
     */
    @GetMapping
    public ResponseEntity<RestResponse<ResultPaginationDTO>> getCompany(@Filter Specification<Company> spec, Pageable pageable) {
        ResultPaginationDTO result = this.companyService.handleGetCompany(spec, pageable);
        return RestResponse.ok(result, "Fetch companies successfully");
    }

    /**
     * Get company by ID - Public endpoint
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<Company>> getCompanyById(@PathVariable("id") long id) {
        Optional<Company> company = this.companyService.findById(id);
        return RestResponse.ok(company.get(), "Fetch company by id successfully");
    }

    /**
     * Update company - Only HR and ADMIN can update
     */
    @PutMapping
    @RequireRole({"ROLE_HR", "ROLE_ADMIN"})
    public ResponseEntity<RestResponse<Company>> updateCompany(@Valid @RequestBody Company reqCompany) {
        log.info("User {} is updating company ID: {}", SecurityUtil.getCurrentUserInfo(), reqCompany.getId());
        Company updatedCompany = this.companyService.handleUpdateCompany(reqCompany);
        return RestResponse.ok(updatedCompany, "Update company successfully");
    }

    /**
     * Delete company - Only ADMIN can delete (strict permission)
     */
    @DeleteMapping("/{id}")
    @RequireRole({"ROLE_ADMIN"})
    public ResponseEntity<RestResponse<Void>> deleteCompany(@PathVariable("id") long id) {
        log.warn("User {} is attempting to delete company ID: {}", SecurityUtil.getCurrentUserInfo(), id);
        this.companyService.handleDeleteCompany(id);
        return RestResponse.ok(null, "Delete company successfully");
    }
}

