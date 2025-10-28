package vn.hoidanit.companyservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import vn.hoidanit.companyservice.domain.Company;
import vn.hoidanit.companyservice.repository.CompanyRepository;

import java.time.Instant;

/**
 * Database Seeder for Company Service
 * Creates sample companies for development and testing ONLY
 * <p>
 * This seeder runs ONLY in dev/test profiles, NOT in production!
 * Production environments should start with an empty companies table.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder {

    private final CompanyRepository companyRepository;

    /**
     * Seed sample companies for development/testing
     * Only runs in 'dev' and 'test' profiles
     */
    @Bean
    @Profile({"dev", "test", "docker"})
    public CommandLineRunner seedCompanies() {
        return args -> {
            log.info("🌱 COMPANY SERVICE - Starting database seeding...");

            // Check if companies already exist
            if (companyRepository.count() > 0) {
                log.info("✅ Companies already exist, skipping seeding");
                return;
            }

            // Create sample companies with logos from MinIO
            createCompany(
                    "Google Vietnam",
                    "Leading technology company specializing in search, cloud computing, and advertising",
                    "District 1, Ho Chi Minh City",
                    "1716687909879-google.png"
            );

            createCompany(
                    "Amazon Vietnam",
                    "E-commerce and cloud computing giant offering retail and AWS services",
                    "Hanoi, Vietnam",
                    "1716687538974-amzon.jpg"
            );

            createCompany(
                    "Microsoft Vietnam",
                    "Software and technology services provider, creator of Windows and Office",
                    "District 7, Ho Chi Minh City",
                    "1716688067540-microsoft.png"
            );

            createCompany(
                    "Apple Vietnam",
                    "Consumer electronics and software company known for iPhone, iPad, and Mac",
                    "Hanoi, Vietnam",
                    "1716687768336-apple.jpg"
            );

            createCompany(
                    "Netflix Vietnam",
                    "Streaming entertainment service providing movies and TV shows",
                    "Ho Chi Minh City",
                    "1716688067538-netflix.png"
            );

            createCompany(
                    "Shopee Vietnam",
                    "Leading e-commerce platform in Southeast Asia",
                    "District 7, Ho Chi Minh City",
                    "1716688292011-shopee.png"
            );

            createCompany(
                    "Lazada Vietnam",
                    "E-commerce platform offering wide range of products",
                    "Hanoi, Vietnam",
                    "1716688017004-lazada.png"
            );

            createCompany(
                    "Tiki Vietnam",
                    "Online shopping platform and book store",
                    "Ho Chi Minh City",
                    "1716688336563-tiki.jpg"
            );

            log.info("🎉 COMPANY SERVICE - Database seeding completed successfully!");
        };
    }

    private void createCompany(String name, String description, String address, String logo) {
        try {
            Company company = new Company();
            company.setName(name);
            company.setDescription(description);
            company.setAddress(address);
            company.setLogo(logo);
            company.setCreatedBy("system");
            company.setCreatedAt(Instant.now());

            companyRepository.save(company);
            log.info("✅ Created company: {}", name);
        } catch (Exception e) {
            log.error("❌ Failed to create company: {} | Error: {}", name, e.getMessage());
        }
    }
}

