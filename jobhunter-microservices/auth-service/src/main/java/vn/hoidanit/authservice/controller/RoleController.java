package vn.hoidanit.authservice.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.authservice.domain.Role;
import vn.hoidanit.authservice.domain.dto.ResultPaginationDTO;
import vn.hoidanit.authservice.service.RoleService;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Role> create(@Valid @RequestBody Role role) {
        if (this.roleService.existByName(role.getName())) {
            throw new RuntimeException("Role với name = " + role.getName() + " đã tồn tại");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.roleService.create(role));
    }

    @PutMapping
    public ResponseEntity<Role> update(@Valid @RequestBody Role role) {
        if (this.roleService.fetchById(role.getId()) == null) {
            throw new RuntimeException("Role với id = " + role.getId() + " không tồn tại");
        }

        return ResponseEntity.ok(this.roleService.update(role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        if (this.roleService.fetchById(id) == null) {
            throw new RuntimeException("Role với id = " + id + " không tồn tại");
        }
        this.roleService.delete(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getRoles(
            Specification<Role> spec,
            Pageable pageable) {
        return ResponseEntity.ok(this.roleService.getRoles(spec, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getById(@PathVariable("id") long id) {
        Role role = this.roleService.fetchById(id);
        if (role == null) {
            throw new RuntimeException("Role với id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok(role);
    }
}
