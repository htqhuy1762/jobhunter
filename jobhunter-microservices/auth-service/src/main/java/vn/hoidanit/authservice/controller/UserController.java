package vn.hoidanit.authservice.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.authservice.annotation.PageableDefault;
import vn.hoidanit.authservice.annotation.RateLimit;
import vn.hoidanit.authservice.domain.User;
import vn.hoidanit.authservice.domain.dto.ResCreateUserDTO;
import vn.hoidanit.authservice.domain.dto.ResUpdateUserDTO;
import vn.hoidanit.authservice.domain.dto.ResUserDTO;
import vn.hoidanit.authservice.domain.dto.ResultPaginationDTO;
import vn.hoidanit.authservice.domain.response.RestResponse;
import vn.hoidanit.authservice.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @RateLimit(name = "createUser")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<ResCreateUserDTO>> createNewUser(@Valid @RequestBody User user) {
        boolean isEmailExist = this.userService.isEmailExist(user.getEmail());
        if (isEmailExist) {
            throw new RuntimeException("Email " + user.getEmail() + " đã tồn tại");
        }

        User newUser = this.userService.handleCreateUser(user);
        ResCreateUserDTO userDTO = this.userService.convertToResCreateUserDTO(newUser);
        return RestResponse.created(userDTO, "Create user successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> deleteUser(@PathVariable("id") long id) {
        User currentUser = this.userService.handleGetUserById(id);
        if (currentUser == null) {
            throw new RuntimeException("User với id = " + id + " không tồn tại");
        }

        this.userService.handleDeleteUser(id);
        return RestResponse.ok(null, "Delete user successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<RestResponse<ResUserDTO>> getUserById(@PathVariable("id") long id) {
        User user = this.userService.handleGetUserById(id);
        if (user == null) {
            throw new RuntimeException("User với id = " + id + " không tồn tại");
        }

        ResUserDTO userDTO = this.userService.convertToResUserDTO(user);
        return RestResponse.ok(userDTO, "Fetch user by id successfully");
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<RestResponse<ResultPaginationDTO>> getAllUser(
            @Filter Specification<User> spec,
            @PageableDefault(page = 1, size = 10, sort = "id", direction = "desc") Pageable pageable) {

        ResultPaginationDTO result = this.userService.handleGetAllUser(spec, pageable);
        return RestResponse.ok(result, "Fetch users successfully");
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<ResUpdateUserDTO>> updateUser(@RequestBody User user) {
        User updatedUser = this.userService.handleUpdateUser(user);
        if (updatedUser == null) {
            throw new RuntimeException("User với id = " + user.getId() + " không tồn tại");
        }

        ResUpdateUserDTO userDTO = this.userService.convertToResUpdateUserDTO(updatedUser);
        return RestResponse.ok(userDTO, "Update user successfully");
    }
}
