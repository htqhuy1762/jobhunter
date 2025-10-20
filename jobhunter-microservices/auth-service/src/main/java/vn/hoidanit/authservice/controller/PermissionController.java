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
import vn.hoidanit.authservice.service.PermissionService;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<Permission> create(@Valid @RequestBody Permission permission) {
        if (this.permissionService.isPermissionExist(permission)) {
            throw new RuntimeException("Permission đã tồn tại");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.permissionService.create(permission));
    }

    @PutMapping
    public ResponseEntity<Permission> update(@Valid @RequestBody Permission permission) {
        if (this.permissionService.fetchById(permission.getId()) == null) {
            throw new RuntimeException("Permission với id = " + permission.getId() + " không tồn tại");
        }

        if (this.permissionService.isPermissionExist(permission)) {
            throw new RuntimeException("Permission đã tồn tại");
        }

        return ResponseEntity.ok(this.permissionService.update(permission));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        if (this.permissionService.fetchById(id) == null) {
            throw new RuntimeException("Permission với id = " + id + " không tồn tại");
        }
        this.permissionService.delete(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getPermissions(
            Specification<Permission> spec,
            Pageable pageable) {
        return ResponseEntity.ok(this.permissionService.getPermissions(spec, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permission> getById(@PathVariable("id") long id) {
        Permission permission = this.permissionService.fetchById(id);
        if (permission == null) {
            throw new RuntimeException("Permission với id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok(permission);
    }
}
