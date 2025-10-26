package vn.hoidanit.companyservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.hoidanit.companyservice.domain.Company;
import vn.hoidanit.companyservice.repository.CompanyRepository;

import java.time.Instant;

/**
 * Database Seeder for Company Service
 * Automatically creates sample companies when application starts
 * Run once when application starts
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final CompanyRepository companyRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== COMPANY SERVICE - DATABASE SEEDER START ===");

        // Create sample companies based on SQL dump data
        createCompanyIfNotExists(
            "Google Vietnam",
            "Leading technology company specializing in search, cloud computing, and advertising",
            "District 1, Ho Chi Minh City",
            "1716687909879-google.png"
        );

        createCompanyIfNotExists(
            "Amazon Vietnam",
            "E-commerce and cloud computing giant offering retail and AWS services",
            "Hanoi, Vietnam",
            "1716687538974-amzon.jpg"
        );

        createCompanyIfNotExists(
            "Microsoft Vietnam",
            "Software and technology services provider, creator of Windows and Office",
            "District 7, Ho Chi Minh City",
            "1716688087166-microsoft.png"
        );

        createCompanyIfNotExists(
            "Apple Vietnam",
            "Consumer electronics and software company known for iPhone, iPad, and Mac",
            "Hanoi, Vietnam",
            "1716687768336-apple.jpg"
        );

        createCompanyIfNotExists(
            "Netflix Vietnam",
            "Streaming entertainment service providing movies and TV shows",
            "Ho Chi Minh City",
            "1716688227085-netflix.png"
        );

        createCompanyIfNotExists(
            "Meta (Facebook)",
            "Social media and technology company focusing on connecting people worldwide",
            "District 1, Ho Chi Minh City",
            "1716688370649-meta.png"
        );

        createCompanyIfNotExists(
            "Tesla Vietnam",
            "Electric vehicle and clean energy company revolutionizing automotive industry",
            "Hanoi, Vietnam",
            "1716688554413-tesla.png"
        );

        createCompanyIfNotExists(
            "Shopee Vietnam",
            "Leading e-commerce platform in Southeast Asia",
            "District 7, Ho Chi Minh City",
            "1716688017004-lazada.png"
        );

        log.info("=== COMPANY SERVICE - DATABASE SEEDER COMPLETED ===");
    }

    private void createCompanyIfNotExists(String name, String description, String address, String logo) {
        try {
            // Check if company already exists by name
            boolean exists = companyRepository.findAll().stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(name));

            if (!exists) {
                Company company = new Company();
                company.setName(name);
                company.setDescription(description);
                company.setAddress(address);
                company.setLogo(logo);
                company.setCreatedBy("system");
                company.setCreatedAt(Instant.now());

                companyRepository.save(company);
                log.info("✅ Created company: {}", name);
            } else {
                log.info("ℹ️ Company already exists: {}", name);
            }
        } catch (Exception e) {
            log.error("❌ Failed to create company: {} | Error: {}", name, e.getMessage(), e);
        }
    }
}

