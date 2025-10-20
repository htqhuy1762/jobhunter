package vn.hoidanit.authservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.authservice.domain.Permission;
import vn.hoidanit.authservice.domain.Role;
import vn.hoidanit.authservice.domain.dto.ResultPaginationDTO;
import vn.hoidanit.authservice.repository.PermissionRepository;
import vn.hoidanit.authservice.repository.RoleRepository;

@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public boolean existByName(String name) {
        return this.roleRepository.existsByName(name);
    }

    public Role fetchById(long id) {
        return this.roleRepository.findById(id).orElse(null);
    }

    public Role create(Role role) {
        // Check permissions exist
        if (role.getPermissions() != null) {
            List<Long> reqPermissions = role.getPermissions()
                    .stream().map(Permission::getId)
                    .toList();

            List<Permission> dbPermissions = this.permissionRepository.findAllById(reqPermissions);
            role.setPermissions(dbPermissions);
        }

        return this.roleRepository.save(role);
    }

    public Role update(Role role) {
        Role roleDB = this.fetchById(role.getId());
        if (roleDB != null) {
            // Check permissions exist
            if (role.getPermissions() != null) {
                List<Long> reqPermissions = role.getPermissions()
                        .stream().map(Permission::getId)
                        .toList();

                List<Permission> dbPermissions = this.permissionRepository.findAllById(reqPermissions);
                role.setPermissions(dbPermissions);
            }

            roleDB.setName(role.getName());
            roleDB.setDescription(role.getDescription());
            roleDB.setActive(role.isActive());
            roleDB.setPermissions(role.getPermissions());

            roleDB = this.roleRepository.save(roleDB);
        }
        return roleDB;
    }

    public void delete(long id) {
        this.roleRepository.deleteById(id);
    }

    public ResultPaginationDTO getRoles(Specification<Role> spec, Pageable pageable) {
        Page<Role> pageRole = this.roleRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageRole.getTotalPages());
        mt.setTotal(pageRole.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(pageRole.getContent());
        return rs;
    }
}

