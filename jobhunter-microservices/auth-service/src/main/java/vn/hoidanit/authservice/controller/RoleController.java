package vn.hoidanit.authservice.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.authservice.annotation.PageableDefault;
import vn.hoidanit.authservice.domain.Role;
import vn.hoidanit.authservice.domain.dto.ResultPaginationDTO;
import vn.hoidanit.authservice.domain.response.RestResponse;
import vn.hoidanit.authservice.service.RoleService;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RestResponse<Role>> create(@Valid @RequestBody Role role) {
        if (this.roleService.existByName(role.getName())) {
            throw new RuntimeException("Role với name = " + role.getName() + " đã tồn tại");
        }

        Role createdRole = this.roleService.create(role);
        return RestResponse.created(createdRole, "Create role successfully");
    }

    @PutMapping
    public ResponseEntity<RestResponse<Role>> update(@Valid @RequestBody Role role) {
        if (this.roleService.fetchById(role.getId()) == null) {
            throw new RuntimeException("Role với id = " + role.getId() + " không tồn tại");
        }

        Role updatedRole = this.roleService.update(role);
        return RestResponse.ok(updatedRole, "Update role successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> delete(@PathVariable("id") long id) {
        if (this.roleService.fetchById(id) == null) {
            throw new RuntimeException("Role với id = " + id + " không tồn tại");
        }
        this.roleService.delete(id);
        return RestResponse.ok(null, "Delete role successfully");
    }

    @GetMapping
    public ResponseEntity<RestResponse<ResultPaginationDTO>> getRoles(
            @Filter Specification<Role> spec,
            @PageableDefault(page = 1, size = 10, sort = "id", direction = "desc") Pageable pageable) {

        ResultPaginationDTO result = this.roleService.getRoles(spec, pageable);
        return RestResponse.ok(result, "Fetch roles successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<Role>> getById(@PathVariable("id") long id) {
        Role role = this.roleService.fetchById(id);
        if (role == null) {
            throw new RuntimeException("Role với id = " + id + " không tồn tại");
        }
        return RestResponse.ok(role, "Fetch role by id successfully");
    }
}
