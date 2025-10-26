package vn.hoidanit.authservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.hoidanit.authservice.domain.Permission;
import vn.hoidanit.authservice.domain.Role;
import vn.hoidanit.authservice.domain.User;
import vn.hoidanit.authservice.repository.PermissionRepository;
import vn.hoidanit.authservice.repository.RoleRepository;
import vn.hoidanit.authservice.repository.UserRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== AUTH SERVICE - DATABASE SEEDER START ===");

        // Step 1: Create permissions first
        createPermissions();

        // Step 2: Create roles
        createRoleIfNotExists("ROLE_ADMIN", "Administrator with full access");
        createRoleIfNotExists("ROLE_USER", "Regular user with limited access");
        createRoleIfNotExists("ROLE_HR", "HR manager");

        // Step 3: Assign permissions to roles
        assignPermissionsToAdmin();

        // Step 4: Create default users
        createUserIfNotExists("admin@gmail.com", "123456", "Admin User", "ROLE_ADMIN", 30, User.GenderEnum.MALE);
        createUserIfNotExists("user@gmail.com", "123456", "Test User", "ROLE_USER", 25, User.GenderEnum.FEMALE);
        createUserIfNotExists("hr@gmail.com", "123456", "HR Manager", "ROLE_HR", 35, User.GenderEnum.MALE);

        log.info("=== AUTH SERVICE - DATABASE SEEDER COMPLETED ===");
    }

    private void createPermissions() {
        log.info("--- Creating Permissions ---");

        // USER MODULE
        createPermissionIfNotExists("CREATE_USER", "/api/v1/users", "POST", "USER");
        createPermissionIfNotExists("UPDATE_USER", "/api/v1/users/*", "PUT", "USER");
        createPermissionIfNotExists("DELETE_USER", "/api/v1/users/*", "DELETE", "USER");
        createPermissionIfNotExists("VIEW_USER", "/api/v1/users", "GET", "USER");

        // COMPANY MODULE
        createPermissionIfNotExists("CREATE_COMPANY", "/api/v1/companies", "POST", "COMPANY");
        createPermissionIfNotExists("UPDATE_COMPANY", "/api/v1/companies/*", "PUT", "COMPANY");
        createPermissionIfNotExists("DELETE_COMPANY", "/api/v1/companies/*", "DELETE", "COMPANY");
        createPermissionIfNotExists("VIEW_COMPANY", "/api/v1/companies", "GET", "COMPANY");

        // JOB MODULE
        createPermissionIfNotExists("CREATE_JOB", "/api/v1/jobs", "POST", "JOB");
        createPermissionIfNotExists("UPDATE_JOB", "/api/v1/jobs/*", "PUT", "JOB");
        createPermissionIfNotExists("DELETE_JOB", "/api/v1/jobs/*", "DELETE", "JOB");
        createPermissionIfNotExists("VIEW_JOB", "/api/v1/jobs", "GET", "JOB");

        // RESUME MODULE
        createPermissionIfNotExists("CREATE_RESUME", "/api/v1/resumes", "POST", "RESUME");
        createPermissionIfNotExists("UPDATE_RESUME", "/api/v1/resumes/*", "PUT", "RESUME");
        createPermissionIfNotExists("DELETE_RESUME", "/api/v1/resumes/*", "DELETE", "RESUME");
        createPermissionIfNotExists("VIEW_RESUME", "/api/v1/resumes", "GET", "RESUME");

        // PERMISSION MODULE
        createPermissionIfNotExists("CREATE_PERMISSION", "/api/v1/permissions", "POST", "PERMISSION");
        createPermissionIfNotExists("UPDATE_PERMISSION", "/api/v1/permissions/*", "PUT", "PERMISSION");
        createPermissionIfNotExists("DELETE_PERMISSION", "/api/v1/permissions/*", "DELETE", "PERMISSION");
        createPermissionIfNotExists("VIEW_PERMISSION", "/api/v1/permissions", "GET", "PERMISSION");

        // ROLE MODULE
        createPermissionIfNotExists("CREATE_ROLE", "/api/v1/roles", "POST", "ROLE");
        createPermissionIfNotExists("UPDATE_ROLE", "/api/v1/roles/*", "PUT", "ROLE");
        createPermissionIfNotExists("DELETE_ROLE", "/api/v1/roles/*", "DELETE", "ROLE");
        createPermissionIfNotExists("VIEW_ROLE", "/api/v1/roles", "GET", "ROLE");

        // FILE MODULE
        createPermissionIfNotExists("UPLOAD_FILE", "/api/v1/files", "POST", "FILE");
        createPermissionIfNotExists("DELETE_FILE", "/api/v1/files/*", "DELETE", "FILE");
        createPermissionIfNotExists("VIEW_FILE", "/api/v1/files", "GET", "FILE");

        // SUBSCRIBER MODULE (Notification)
        createPermissionIfNotExists("CREATE_SUBSCRIBER", "/api/v1/subscribers", "POST", "SUBSCRIBER");
        createPermissionIfNotExists("UPDATE_SUBSCRIBER", "/api/v1/subscribers/*", "PUT", "SUBSCRIBER");
        createPermissionIfNotExists("DELETE_SUBSCRIBER", "/api/v1/subscribers/*", "DELETE", "SUBSCRIBER");
        createPermissionIfNotExists("VIEW_SUBSCRIBER", "/api/v1/subscribers", "GET", "SUBSCRIBER");

        log.info("--- Permissions created: {} total ---", permissionRepository.count());
    }

    private void createPermissionIfNotExists(String name, String apiPath, String method, String module) {
        if (!permissionRepository.existsByName(name)) {
            Permission permission = new Permission();
            permission.setName(name);
            permission.setApiPath(apiPath);
            permission.setMethod(method);
            permission.setModule(module);
            permission.setCreatedBy("system");
            permission.setCreatedAt(Instant.now());
            permissionRepository.save(permission);
            log.info("✅ Created permission: {} | Module: {}", name, module);
        } else {
            log.info("ℹ️ Permission already exists: {}", name);
        }
    }

    private void assignPermissionsToAdmin() {
        log.info("--- Assigning Permissions to ADMIN Role ---");

        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if (adminRole == null) {
            log.warn("⚠️ ROLE_ADMIN not found, cannot assign permissions");
            return;
        }

        // Get all permissions
        List<Permission> allPermissions = permissionRepository.findAll();

        // Assign all permissions to ADMIN
        adminRole.setPermissions(allPermissions);
        roleRepository.save(adminRole);

        log.info("✅ Assigned {} permissions to ROLE_ADMIN", allPermissions.size());
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

