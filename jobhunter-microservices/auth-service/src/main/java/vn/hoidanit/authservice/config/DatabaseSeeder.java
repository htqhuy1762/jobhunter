package vn.hoidanit.authservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.hoidanit.authservice.domain.Role;
import vn.hoidanit.authservice.domain.User;
import vn.hoidanit.authservice.domain.User.GenderEnum;
import vn.hoidanit.authservice.repository.RoleRepository;
import vn.hoidanit.authservice.repository.UserRepository;

/**
 * Database Seeder for Development/Testing Environments
 *
 * This seeder creates initial users with properly hashed passwords.
 * It runs ONLY in dev/test profiles, NOT in production!
 *
 * Why separate seeder for users?
 * 1. Password hashing uses BCrypt from Spring Security
 * 2. BCrypt config (rounds) might change
 * 3. Cannot hard-code hash in SQL scripts
 * 4. Need application context for PasswordEncoder
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Seed initial users for development
     * Only runs in 'dev', 'test', and 'docker' profiles
     */
    @Bean
    @Profile({"dev", "test", "docker"})
    public CommandLineRunner seedUsers() {
        return args -> {
            log.info("🌱 Starting database seeding...");

            // Check if users already exist
            if (userRepository.count() > 0) {
                log.info("✅ Users already exist, skipping seeding");
                return;
            }

            // Seed admin user
            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole == null) {
                throw new RuntimeException("ROLE_ADMIN not found! Run SQL init scripts first.");
            }

            User admin = new User();
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("123456")); // ← Uses current BCrypt config
            admin.setName("Admin User");
            admin.setAge(30);
            admin.setGender(GenderEnum.MALE);
            admin.setAddress("Hanoi, Vietnam");
            admin.setRole(adminRole);
            userRepository.save(admin);
            log.info("✅ Created admin user: admin@gmail.com");

            // Seed regular user
            Role userRole = roleRepository.findByName("ROLE_USER");
            if (userRole == null) {
                throw new RuntimeException("ROLE_USER not found!");
            }

            User regularUser = new User();
            regularUser.setEmail("user@gmail.com");
            regularUser.setPassword(passwordEncoder.encode("123456"));
            regularUser.setName("Test User");
            regularUser.setAge(25);
            regularUser.setGender(GenderEnum.FEMALE);
            regularUser.setAddress("Ho Chi Minh, Vietnam");
            regularUser.setRole(userRole);
            userRepository.save(regularUser);
            log.info("✅ Created regular user: user@gmail.com");

            // Seed HR user
            Role hrRole = roleRepository.findByName("ROLE_HR");
            if (hrRole == null) {
                throw new RuntimeException("ROLE_HR not found!");
            }

            User hrUser = new User();
            hrUser.setEmail("hr@gmail.com");
            hrUser.setPassword(passwordEncoder.encode("123456"));
            hrUser.setName("HR Manager");
            hrUser.setAge(35);
            hrUser.setGender(GenderEnum.MALE);
            hrUser.setAddress("Da Nang, Vietnam");
            hrUser.setRole(hrRole);
            userRepository.save(hrUser);
            log.info("✅ Created HR user: hr@gmail.com");

            log.info("🎉 Database seeding completed successfully!");
        };
    }

    /**
     * Seed production users (if needed)
     * Only runs in 'prod' profile
     * Should create minimal admin accounts only
     */
    @Bean
    @Profile("prod")
    public CommandLineRunner seedProductionUsers() {
        return args -> {
            log.info("🔒 Checking production admin account...");

            // Only create admin if doesn't exist
            User existingAdmin = userRepository.findByEmail("admin@production.com");
            if (existingAdmin != null) {
                log.info("✅ Production admin already exists");
                return;
            }

            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole == null) {
                throw new RuntimeException("ROLE_ADMIN not found!");
            }

            String adminPassword = System.getenv("ADMIN_PASSWORD");
            if (adminPassword == null || adminPassword.isEmpty()) {
                adminPassword = "ChangeMe123!"; // Default fallback
                log.warn("⚠️  ADMIN_PASSWORD env var not set, using default password!");
            }

            User admin = new User();
            admin.setEmail("admin@production.com");
            admin.setPassword(passwordEncoder.encode(adminPassword)); // ← From env var
            admin.setName("Production Admin");
            admin.setRole(adminRole);
            userRepository.save(admin);

            log.info("✅ Production admin created");
            log.warn("⚠️  Please change the admin password immediately!");
        };
    }
}

