package vn.hoidanit.authservice.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.authservice.domain.Permission;
import vn.hoidanit.authservice.domain.dto.ResultPaginationDTO;
import vn.hoidanit.authservice.domain.response.RestResponse;
import vn.hoidanit.authservice.service.PermissionService;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<RestResponse<Permission>> create(@Valid @RequestBody Permission permission) {
        if (this.permissionService.isPermissionExist(permission)) {
            throw new RuntimeException("Permission đã tồn tại");
        }

        Permission createdPermission = this.permissionService.create(permission);
        return RestResponse.created(createdPermission, "Create permission successfully");
    }

    @PutMapping
    public ResponseEntity<RestResponse<Permission>> update(@Valid @RequestBody Permission permission) {
        if (this.permissionService.fetchById(permission.getId()) == null) {
            throw new RuntimeException("Permission với id = " + permission.getId() + " không tồn tại");
        }

        if (this.permissionService.isPermissionExist(permission)) {
            throw new RuntimeException("Permission đã tồn tại");
        }

        Permission updatedPermission = this.permissionService.update(permission);
        return RestResponse.ok(updatedPermission, "Update permission successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> delete(@PathVariable("id") long id) {
        if (this.permissionService.fetchById(id) == null) {
            throw new RuntimeException("Permission với id = " + id + " không tồn tại");
        }
        this.permissionService.delete(id);
        return RestResponse.ok(null, "Delete permission successfully");
    }

    @GetMapping
    public ResponseEntity<RestResponse<ResultPaginationDTO>> getPermissions(
            Specification<Permission> spec,
            Pageable pageable) {
        ResultPaginationDTO result = this.permissionService.getPermissions(spec, pageable);
        return RestResponse.ok(result, "Fetch permissions successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<Permission>> getById(@PathVariable("id") long id) {
        Permission permission = this.permissionService.fetchById(id);
        if (permission == null) {
            throw new RuntimeException("Permission với id = " + id + " không tồn tại");
        }
        return RestResponse.ok(permission, "Fetch permission by id successfully");
    }
}
