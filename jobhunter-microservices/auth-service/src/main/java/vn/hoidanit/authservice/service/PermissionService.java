package vn.hoidanit.authservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.authservice.domain.Permission;
import vn.hoidanit.authservice.domain.dto.ResultPaginationDTO;
import vn.hoidanit.authservice.repository.PermissionRepository;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public boolean isPermissionExist(Permission p) {
        return permissionRepository.existsByModuleAndApiPathAndMethod(
                p.getModule(),
                p.getApiPath(),
                p.getMethod());
    }

    public Permission fetchById(long id) {
        return this.permissionRepository.findById(id).orElse(null);
    }

    public Permission create(Permission p) {
        return this.permissionRepository.save(p);
    }

    public Permission update(Permission p) {
        Permission permissionDB = this.fetchById(p.getId());
        if (permissionDB != null) {
            permissionDB.setName(p.getName());
            permissionDB.setApiPath(p.getApiPath());
            permissionDB.setMethod(p.getMethod());
            permissionDB.setModule(p.getModule());

            permissionDB = this.permissionRepository.save(permissionDB);
        }
        return permissionDB;
    }

    public void delete(long id) {
        this.permissionRepository.deleteById(id);
    }

    public ResultPaginationDTO getPermissions(Specification<Permission> spec, Pageable pageable) {
        Page<Permission> pPermissions = this.permissionRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pPermissions.getTotalPages());
        mt.setTotal(pPermissions.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(pPermissions.getContent());
        return rs;
    }
}

