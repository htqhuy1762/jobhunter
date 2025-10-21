package vn.hoidanit.authservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.hoidanit.authservice.domain.Role;
import vn.hoidanit.authservice.domain.User;
import vn.hoidanit.authservice.repository.RoleRepository;
import vn.hoidanit.authservice.repository.UserRepository;

/**
 * Database Seeder - Automatically creates default users with CORRECT password hashes
 * Run once when application starts
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== DATABASE SEEDER START ===");

        // Create default roles if not exist
        createRoleIfNotExists("ROLE_ADMIN", "Administrator with full access");
        createRoleIfNotExists("ROLE_USER", "Regular user with limited access");
        createRoleIfNotExists("ROLE_HR", "HR manager");

        // Create default users with CORRECT password hash
        createUserIfNotExists("admin@gmail.com", "123456", "Admin User", "ROLE_ADMIN", 30, User.GenderEnum.MALE);
        createUserIfNotExists("user@gmail.com", "123456", "Test User", "ROLE_USER", 25, User.GenderEnum.FEMALE);
        createUserIfNotExists("hr@gmail.com", "123456", "HR Manager", "ROLE_HR", 35, User.GenderEnum.MALE);

        log.info("=== DATABASE SEEDER COMPLETED ===");
    }

    private void createRoleIfNotExists(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            role.setActive(true);
            roleRepository.save(role);
            log.info("Created role: {}", name);
        } else {
            log.info("Role already exists: {}", name);
        }
    }

    private void createUserIfNotExists(String email, String rawPassword, String name,
                                       String roleName, int age, User.GenderEnum gender) {
        try {
            if (!userRepository.existsByEmail(email)) {
                // Hash password using Spring Security PasswordEncoder
                String hashedPassword = passwordEncoder.encode(rawPassword);

                User user = new User();
                user.setEmail(email);
                user.setPassword(hashedPassword);  // Password được hash TỰ ĐỘNG bởi Spring Security
                user.setName(name);
                user.setAge(age);
                user.setGender(gender);

                // Set createdBy manually (avoid SecurityUtil NullPointerException during startup)
                user.setCreatedBy("system");
                user.setCreatedAt(java.time.Instant.now());

                // Set role
                Role role = roleRepository.findByName(roleName);
                if (role != null) {
                    user.setRole(role);
                } else {
                    log.warn("Role not found: {}. User will be created without role.", roleName);
                }

                userRepository.save(user);
                log.info("✅ Created user: {} with role: {} | Password hash: {}",
                         email, roleName, hashedPassword.substring(0, 20) + "...");
            } else {
                // Update password if user exists (để fix password cũ sai)
                User existingUser = userRepository.findByEmail(email);
                if (existingUser != null) {
                    String hashedPassword = passwordEncoder.encode(rawPassword);
                    existingUser.setPassword(hashedPassword);
                    existingUser.setUpdatedBy("system");
                    existingUser.setUpdatedAt(java.time.Instant.now());
                    userRepository.save(existingUser);
                    log.info("✅ Updated password for existing user: {} | New hash: {}",
                             email, hashedPassword.substring(0, 20) + "...");
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to create/update user: {} | Error: {}", email, e.getMessage(), e);
        }
    }
}

