package vn.hoidanit.jobservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.hoidanit.jobservice.domain.Job;
import vn.hoidanit.jobservice.domain.Skill;
import vn.hoidanit.jobservice.repository.JobRepository;
import vn.hoidanit.jobservice.repository.SkillRepository;
import vn.hoidanit.jobservice.util.constant.LevelEnum;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Database Seeder for Job Service
 * Automatically creates sample skills and jobs when application starts
 * Run once when application starts
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== JOB SERVICE - DATABASE SEEDER START ===");

        // Create skills first
        createSkillsIfNotExist();

        // Create sample jobs
        createJobsIfNotExist();

        log.info("=== JOB SERVICE - DATABASE SEEDER COMPLETED ===");
    }

    private void createSkillsIfNotExist() {
        log.info("--- Creating Skills ---");

        String[] skillNames = {
            "Java", "Spring Boot", "MySQL", "PostgreSQL", "MongoDB",
            "Redis", "Docker", "Kubernetes", "AWS", "React",
            "Angular", "Vue.js", "Node.js", "Python", "JavaScript",
            "TypeScript", "Microservices", "REST API", "GraphQL", "Git"
        };

        for (String skillName : skillNames) {
            try {
                if (!skillRepository.existsByName(skillName)) {
                    Skill skill = new Skill();
                    skill.setName(skillName);
                    skill.setCreatedBy("system");
                    skill.setCreatedAt(Instant.now());
                    skillRepository.save(skill);
                    log.info("✅ Created skill: {}", skillName);
                } else {
                    log.info("ℹ️ Skill already exists: {}", skillName);
                }
            } catch (Exception e) {
                log.error("❌ Failed to create skill: {} | Error: {}", skillName, e.getMessage());
            }
        }
    }

    private void createJobsIfNotExist() {
        log.info("--- Creating Jobs ---");

        // Job 1: Backend Developer
        createJobIfNotExists(
            "Backend Developer",
            "Hanoi",
            2000.00,
            5,
            LevelEnum.JUNIOR,
            "Looking for talented Java Spring Boot developers to join our growing team. You will work on building scalable microservices and RESTful APIs.",
            1L,
            Arrays.asList("Java", "Spring Boot", "MySQL", "Docker")
        );

        // Job 2: Frontend Developer
        createJobIfNotExists(
            "Frontend Developer",
            "Ho Chi Minh City",
            1800.00,
            3,
            LevelEnum.MIDDLE,
            "Seeking React developer with strong JavaScript skills. You will create modern, responsive web applications.",
            2L,
            Arrays.asList("React", "JavaScript", "TypeScript", "REST API")
        );

        // Job 3: Full Stack Developer
        createJobIfNotExists(
            "Full Stack Developer",
            "Da Nang",
            2500.00,
            2,
            LevelEnum.SENIOR,
            "Full stack developer with expertise in both Java backend and React frontend. Lead technical decisions and mentor junior developers.",
            3L,
            Arrays.asList("Java", "Spring Boot", "React", "MySQL", "Docker", "Microservices")
        );

        // Job 4: DevOps Engineer
        createJobIfNotExists(
            "DevOps Engineer",
            "Hanoi",
            2800.00,
            2,
            LevelEnum.SENIOR,
            "Experience with Docker, Kubernetes, and AWS required. Build and maintain CI/CD pipelines and cloud infrastructure.",
            1L,
            Arrays.asList("Docker", "Kubernetes", "AWS", "Git", "MySQL")
        );

        // Job 5: Mobile Developer
        createJobIfNotExists(
            "Mobile Developer",
            "Ho Chi Minh City",
            2200.00,
            4,
            LevelEnum.MIDDLE,
            "iOS/Android development using modern frameworks. Create engaging mobile experiences for millions of users.",
            4L,
            Arrays.asList("React", "JavaScript", "TypeScript", "REST API")
        );

        // Job 6: Python Developer
        createJobIfNotExists(
            "Python Developer",
            "Hanoi",
            2100.00,
            3,
            LevelEnum.MIDDLE,
            "Python developer for data processing and API development. Work with Django/Flask and integrate with various databases.",
            5L,
            Arrays.asList("Python", "PostgreSQL", "MongoDB", "REST API", "Docker")
        );

        // Job 7: Cloud Architect
        createJobIfNotExists(
            "Cloud Architect",
            "Ho Chi Minh City",
            3500.00,
            1,
            LevelEnum.SENIOR,
            "Design and implement cloud-native solutions on AWS. Lead migration projects and optimize cloud infrastructure costs.",
            1L,
            Arrays.asList("AWS", "Docker", "Kubernetes", "Microservices", "PostgreSQL")
        );

        // Job 8: Angular Developer
        createJobIfNotExists(
            "Angular Developer",
            "Da Nang",
            1900.00,
            3,
            LevelEnum.MIDDLE,
            "Build enterprise applications using Angular framework. Strong understanding of TypeScript and RxJS required.",
            3L,
            Arrays.asList("Angular", "TypeScript", "JavaScript", "REST API")
        );
    }

    private void createJobIfNotExists(String name, String location, double salary, int quantity,
                                      LevelEnum level, String description, Long companyId,
                                      List<String> skillNames) {
        try {
            // Check if job already exists by name
            boolean exists = jobRepository.findAll().stream()
                    .anyMatch(j -> j.getName().equalsIgnoreCase(name));

            if (!exists) {
                Job job = new Job();
                job.setName(name);
                job.setLocation(location);
                job.setSalary(salary);
                job.setQuantity(quantity);
                job.setLevel(level);
                job.setDescription(description);
                job.setCompanyId(companyId);
                job.setActive(true);
                job.setStartDate(Instant.now());
                job.setEndDate(Instant.now().plus(90, ChronoUnit.DAYS)); // 3 months from now
                job.setCreatedBy("system");
                job.setCreatedAt(Instant.now());

                // Find and set skills
                List<Skill> skills = new ArrayList<>();
                for (String skillName : skillNames) {
                    skillRepository.findAll().stream()
                            .filter(s -> s.getName().equalsIgnoreCase(skillName))
                            .findFirst()
                            .ifPresent(skills::add);
                }
                job.setSkills(skills);

                jobRepository.save(job);
                log.info("✅ Created job: {} | Location: {} | Level: {} | Skills: {}",
                        name, location, level, skillNames.size());
            } else {
                log.info("ℹ️ Job already exists: {}", name);
            }
        } catch (Exception e) {
            log.error("❌ Failed to create job: {} | Error: {}", name, e.getMessage(), e);
        }
    }
}

