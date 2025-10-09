package vn.hoidanit.jobhunter.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.jobhunter.domain.Permission;
import vn.hoidanit.jobhunter.domain.Role;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.repository.PermissionRepository;
import vn.hoidanit.jobhunter.repository.RoleRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.util.constant.GenderEnum;

@Service
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE");
        long countPermissions = this.permissionRepository.count();
        long countRoles = this.roleRepository.count();
        long countUsers = this.userRepository.count();

        if (countPermissions == 0) {
            ArrayList<Permission> arr = new ArrayList<>();
            arr.add(new Permission("Create a company", "/api/v1/companies", "POST", "COMPANIES"));
            arr.add(new Permission("Update a company", "/api/v1/companies", "PUT", "COMPANIES"));
            arr.add(new Permission("Delete a company", "/api/v1/companies/{id}", "DELETE", "COMPANIES"));
            arr.add(new Permission("Get a company by id", "/api/v1/companies/{id}", "GET", "COMPANIES"));
            arr.add(new Permission("Get companies with pagination", "/api/v1/companies", "GET", "COMPANIES"));

            arr.add(new Permission("Create a job", "/api/v1/jobs", "POST", "JOBS"));
            arr.add(new Permission("Update a job", "/api/v1/jobs", "PUT", "JOBS"));
            arr.add(new Permission("Delete a job", "/api/v1/jobs/{id}", "DELETE", "JOBS"));
            arr.add(new Permission("Get a job by id", "/api/v1/jobs/{id}", "GET", "JOBS"));
            arr.add(new Permission("Get jobs with pagination", "/api/v1/jobs", "GET", "JOBS"));

            arr.add(new Permission("Create a permission", "/api/v1/permissions", "POST", "PERMISSIONS"));
            arr.add(new Permission("Update a permission", "/api/v1/permissions", "PUT", "PERMISSIONS"));
            arr.add(new Permission("Delete a permission", "/api/v1/permissions/{id}", "DELETE", "PERMISSIONS"));
            arr.add(new Permission("Get a permission by id", "/api/v1/permissions/{id}", "GET", "PERMISSIONS"));
            arr.add(new Permission("Get permissions with pagination", "/api/v1/permissions", "GET", "PERMISSIONS"));

            arr.add(new Permission("Create a resume", "/api/v1/resumes", "POST", "RESUMES"));
            arr.add(new Permission("Update a resume", "/api/v1/resumes", "PUT", "RESUMES"));
            arr.add(new Permission("Delete a resume", "/api/v1/resumes/{id}", "DELETE", "RESUMES"));
            arr.add(new Permission("Get a resume by id", "/api/v1/resumes/{id}", "GET", "RESUMES"));
            arr.add(new Permission("Get resumes with pagination", "/api/v1/resumes", "GET", "RESUMES"));

            arr.add(new Permission("Create a role", "/api/v1/roles", "POST", "ROLES"));
            arr.add(new Permission("Update a role", "/api/v1/roles", "PUT", "ROLES"));
            arr.add(new Permission("Delete a role", "/api/v1/roles/{id}", "DELETE", "ROLES"));
            arr.add(new Permission("Get a role by id", "/api/v1/roles/{id}", "GET", "ROLES"));
            arr.add(new Permission("Get roles with pagination", "/api/v1/roles", "GET", "ROLES"));

            arr.add(new Permission("Create a user", "/api/v1/users", "POST", "USERS"));
            arr.add(new Permission("Update a user", "/api/v1/users", "PUT", "USERS"));
            arr.add(new Permission("Delete a user", "/api/v1/users/{id}", "DELETE", "USERS"));
            arr.add(new Permission("Get a user by id", "/api/v1/users/{id}", "GET", "USERS"));
            arr.add(new Permission("Get users with pagination", "/api/v1/users", "GET", "USERS"));

            arr.add(new Permission("Create a subscriber", "/api/v1/subscribers", "POST", "SUBSCRIBERS"));
            arr.add(new Permission("Update a subscriber", "/api/v1/subscribers", "PUT", "SUBSCRIBERS"));
            arr.add(new Permission("Delete a subscriber", "/api/v1/subscribers/{id}", "DELETE", "SUBSCRIBERS"));
            arr.add(new Permission("Get a subscriber by id", "/api/v1/subscribers/{id}", "GET", "SUBSCRIBERS"));
            arr.add(new Permission("Get subscribers with pagination", "/api/v1/subscribers", "GET", "SUBSCRIBERS"));

            arr.add(new Permission("Download a file", "/api/v1/files", "POST", "FILES"));
            arr.add(new Permission("Upload a file", "/api/v1/files", "GET", "FILES"));

            this.permissionRepository.saveAll(arr);
        }

        if (countRoles == 0) {
            System.out.println(">>> Creating default roles...");
            List<Permission> allPermissions = this.permissionRepository.findAll();

            // 1. SUPER_ADMIN - Full permissions
            Role superAdminRole = new Role();
            superAdminRole.setName("SUPER_ADMIN");
            superAdminRole.setDescription("Super Admin - Full system access, can manage everything");
            superAdminRole.setActive(true);
            superAdminRole.setPermissions(allPermissions);
            this.roleRepository.save(superAdminRole);
            System.out.println(">>> Created role: SUPER_ADMIN");

            // 2. ROLE_ADMIN - Can manage users, companies, jobs, all resumes
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setDescription("Admin - Can manage users, companies, jobs, and all resumes");
            adminRole.setActive(true);
            // Admin có hầu hết permissions trừ một số permissions nhạy cảm về roles/permissions
            List<Permission> adminPermissions = allPermissions.stream()
                .filter(p -> !p.getModule().equals("PERMISSIONS")) // Admin không quản lý permissions
                .toList();
            adminRole.setPermissions(adminPermissions);
            this.roleRepository.save(adminRole);
            System.out.println(">>> Created role: ROLE_ADMIN");

            // 3. ROLE_HR - Can manage jobs and resumes for their company
            Role hrRole = new Role();
            hrRole.setName("ROLE_HR");
            hrRole.setDescription("HR - Can manage jobs and resumes for their company");
            hrRole.setActive(true);
            // HR chỉ có quyền với JOBS và RESUMES
            List<Permission> hrPermissions = allPermissions.stream()
                .filter(p -> p.getModule().equals("JOBS") || p.getModule().equals("RESUMES"))
                .toList();
            hrRole.setPermissions(hrPermissions);
            this.roleRepository.save(hrRole);
            System.out.println(">>> Created role: ROLE_HR");

            // 4. ROLE_USER - Normal user, can view jobs and manage their own resumes
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setDescription("Normal User - Can view jobs and manage their own resumes");
            userRole.setActive(true);
            // User chỉ có quyền xem jobs và quản lý CV của mình
            List<Permission> userPermissions = allPermissions.stream()
                .filter(p ->
                    (p.getModule().equals("JOBS") && p.getMethod().equals("GET")) ||
                    (p.getModule().equals("RESUMES") &&
                        (p.getMethod().equals("POST") || p.getMethod().equals("GET")))
                )
                .toList();
            userRole.setPermissions(userPermissions);
            this.roleRepository.save(userRole);
            System.out.println(">>> Created role: ROLE_USER");
        }

        if (countUsers == 0) {
            System.out.println(">>> Creating default admin user...");
            User adminUser = new User();
            adminUser.setEmail("admin@gmail.com");
            adminUser.setAddress("Hanoi, Vietnam");
            adminUser.setAge(25);
            adminUser.setGender(GenderEnum.MALE);
            adminUser.setName("Super Administrator");
            adminUser.setPassword(this.passwordEncoder.encode("123456"));

            Role superAdminRole = this.roleRepository.findByName("SUPER_ADMIN");
            if (superAdminRole != null) {
                adminUser.setRole(superAdminRole);
            }

            this.userRepository.save(adminUser);
            System.out.println(">>> Created default user: admin@gmail.com / 123456");
            System.out.println(">>> ⚠️  REMEMBER TO CHANGE DEFAULT PASSWORD!");
        }

        if (countPermissions > 0 && countRoles > 0 && countUsers > 0) {
            System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA...");
        } else {
            System.out.println(">>> END INIT DATABASE");
            System.out.println(">>> Database initialized successfully with:");
            System.out.println("    - Permissions: " + this.permissionRepository.count());
            System.out.println("    - Roles: " + this.roleRepository.count() + " (SUPER_ADMIN, ROLE_ADMIN, ROLE_HR, ROLE_USER)");
            System.out.println("    - Users: " + this.userRepository.count());
        }
    }

}
