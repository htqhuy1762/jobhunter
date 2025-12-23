package vn.hoidanit.authservice.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.authservice.domain.Role;
import vn.hoidanit.authservice.domain.User;
import vn.hoidanit.authservice.domain.dto.ResCreateUserDTO;
import vn.hoidanit.authservice.domain.dto.ResUpdateUserDTO;
import vn.hoidanit.authservice.domain.dto.ResUserDTO;
import vn.hoidanit.authservice.domain.dto.ResultPaginationDTO;
import vn.hoidanit.authservice.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public User handleCreateUser(User user) {
        // Encode password
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Check role
        if (user.getRole() != null) {
            Role r = this.roleService.fetchById(user.getRole().getId());
            user.setRole(r != null ? r : null);
        }

        return this.userRepository.save(user);
    }

    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public User handleGetUserById(long id) {
        return this.userRepository.findById(id).orElse(null);
    }

    public ResultPaginationDTO handleGetAllUser(Specification<User> spec, Pageable pageable) {
        Page<User> users = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(users.getTotalPages());
        mt.setTotal(users.getTotalElements());

        rs.setMeta(mt);

        List<ResUserDTO> resUsers = users.getContent().stream()
                .map(this::convertToResUserDTO)
                .collect(Collectors.toList());

        rs.setResult(resUsers);
        return rs;
    }

    @CacheEvict(value = {"users", "users:permissions"}, allEntries = true)
    public User handleUpdateUser(User user) {
        User updateUser = this.handleGetUserById(user.getId());
        if (updateUser != null) {
            updateUser.setName(user.getName());
            updateUser.setAddress(user.getAddress());
            updateUser.setAge(user.getAge());
            updateUser.setGender(user.getGender());

            // Check role
            if (user.getRole() != null) {
                Role r = this.roleService.fetchById(user.getRole().getId());
                updateUser.setRole(r != null ? r : null);
            }

            updateUser = this.userRepository.save(updateUser);
        }
        return updateUser;
    }

    @Cacheable(value = "users", key = "#email")
    public User handleGetUserByUsername(String email) {
        return this.userRepository.findByEmail(email);
    }

    @Cacheable(value = "users:permissions", key = "#email")
    public User handleGetUserByUsernameWithPermissions(String email) {
        return this.userRepository.findByEmailWithRoleAndPermissions(email);
    }

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        ResCreateUserDTO res = new ResCreateUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setCreatedAt(user.getCreatedAt());
        
        if (user.getRole() != null) {
            res.setRole(new ResCreateUserDTO.RoleUser(user.getRole().getId(), user.getRole().getName()));
        }
        
        if (user.getCompanyId() != null) {
            res.setCompany(new ResCreateUserDTO.CompanyUser(user.getCompanyId()));
        }
        
        return res;
    }

    public ResUpdateUserDTO convertToResUpdateUserDTO(User user) {
        ResUpdateUserDTO res = new ResUpdateUserDTO();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setUpdatedAt(user.getUpdatedAt());
        
        if (user.getRole() != null) {
            res.setRole(new ResUpdateUserDTO.RoleUser(user.getRole().getId(), user.getRole().getName()));
        }
        
        if (user.getCompanyId() != null) {
            res.setCompany(new ResUpdateUserDTO.CompanyUser(user.getCompanyId()));
        }
        
        return res;
    }

    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO res = new ResUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setCreatedAt(user.getCreatedAt());
        res.setUpdatedAt(user.getUpdatedAt());
        
        if (user.getRole() != null) {
            res.setRole(new ResUserDTO.RoleUser(user.getRole().getId(), user.getRole().getName()));
        }
        
        if (user.getCompanyId() != null) {
            res.setCompany(new ResUserDTO.CompanyUser(user.getCompanyId()));
        }
        
        return res;
    }

    public void updateUserToken(String token, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            // In microservices, we don't store token in DB anymore
            // Token is stored in Redis via TokenService
        }
    }

    public boolean updatePassword(User user, String newPassword) {
        try {
            user.setPassword(passwordEncoder.encode(newPassword));
            this.userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Update user's own profile (self-update)
     * Only allows updating safe fields: name, age, gender, address
     * Does NOT allow changing: email, password, role, company
     */
    @CacheEvict(value = {"users", "users:permissions"}, key = "#email")
    public User handleUpdateOwnProfile(String email, User updateData) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser == null) {
            return null;
        }

        // Only update safe fields
        currentUser.setName(updateData.getName());
        currentUser.setAge(updateData.getAge());
        currentUser.setGender(updateData.getGender());
        currentUser.setAddress(updateData.getAddress());

        return this.userRepository.save(currentUser);
    }
}

