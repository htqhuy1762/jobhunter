package vn.hoidanit.companyservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.companyservice.domain.Company;
import vn.hoidanit.companyservice.dto.ResultPaginationDTO;
import vn.hoidanit.companyservice.repository.CompanyRepository;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    public Optional<Company> findById(long id) {
        return companyRepository.findById(id);
    }

    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    public ResultPaginationDTO getAllCompanies(Specification<Company> spec, Pageable pageable) {
        Page<Company> companyPage = companyRepository.findAll(spec, pageable);
        return buildPaginationResult(companyPage, companyPage.getContent(), pageable);
    }

    public Company updateCompany(Company company) {
        return companyRepository.findById(company.getId())
                .map(existingCompany -> {
                    updateCompanyFields(existingCompany, company);
                    return companyRepository.save(existingCompany);
                })
                .orElse(null);
    }

    public void deleteCompany(long id) {
        companyRepository.deleteById(id);
    }

    private void updateCompanyFields(Company target, Company source) {
        target.setName(source.getName());
        target.setLogo(source.getLogo());
        target.setDescription(source.getDescription());
        target.setAddress(source.getAddress());
    }

    private ResultPaginationDTO buildPaginationResult(Page<?> page, Object content, Pageable pageable) {
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(page.getNumber() + 1);
        meta.setPageSize(page.getSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(content);
        return result;
    }
}

