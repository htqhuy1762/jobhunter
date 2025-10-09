package vn.hoidanit.jobhunter.controller;

import vn.hoidanit.jobhunter.domain.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.RoleService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping("/roles")
    @ApiMessage("Create a role")
    @Secured({"SUPER_ADMIN"})  // Only SUPER_ADMIN can create roles
    public ResponseEntity<Role> create(@Valid @RequestBody Role r) throws IdInvalidException {
        // check name
        if (this.roleService.existByName(r.getName())) {
            throw new IdInvalidException("Role with name = " + r.getName() + " already exists.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.roleService.create(r));
    }

    @PutMapping("/roles")
    @ApiMessage("Update a role")
    @Secured({"SUPER_ADMIN"})  // Only SUPER_ADMIN can update roles
    public ResponseEntity<Role> update(@Valid @RequestBody Role r) throws IdInvalidException {
        // check exist by id
        if (this.roleService.fetchById(r.getId()) == null) {
            throw new IdInvalidException("Role with id = " + r.getId() + " does not exist.");
        }

        // // check exist by name
        // if (this.roleService.existByName(r.getName())) {
        // throw new IdInvalidException("Role with name = " + r.getName() + " already
        // exists.");
        // }

        // update role
        return ResponseEntity.ok().body(this.roleService.update(r));
    }

    @DeleteMapping("/roles/{id}")
    @ApiMessage("Delete a role")
    @Secured({"SUPER_ADMIN"})  // Only SUPER_ADMIN can delete roles
    public ResponseEntity<Void> delete(@PathVariable("id") long id) throws IdInvalidException {
        // check id
        if (this.roleService.fetchById(id) == null) {
            throw new IdInvalidException("Role với id = " + id + " không tồn tại");
        }
        this.roleService.delete(id);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/roles")
    @ApiMessage("Get all roles")
    @Secured({"SUPER_ADMIN", "ROLE_ADMIN"})  // Admin can view roles
    public ResponseEntity<ResultPaginationDTO> getAllRoles(@Filter Specification<Role> spec, Pageable pageable) {
        return ResponseEntity.ok().body(this.roleService.getRoles(spec, pageable));
    }

    @GetMapping("/roles/{id}")
    @ApiMessage("Get a role by id")
    @Secured({"SUPER_ADMIN", "ROLE_ADMIN"})  // Admin can view role details
    public ResponseEntity<Role> getById(@PathVariable("id") long id) throws IdInvalidException {
        Role r = this.roleService.fetchById(id);
        if (r == null) {
            throw new IdInvalidException("Role with id = " + id + " does not exist.");
        }
        return ResponseEntity.ok().body(r);
    }
}
