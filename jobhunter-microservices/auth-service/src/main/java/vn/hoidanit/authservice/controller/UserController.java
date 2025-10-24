package vn.hoidanit.authservice.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping
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
    public ResponseEntity<RestResponse<Void>> deleteUser(@PathVariable("id") long id) {
        User currentUser = this.userService.handleGetUserById(id);
        if (currentUser == null) {
            throw new RuntimeException("User với id = " + id + " không tồn tại");
        }

        this.userService.handleDeleteUser(id);
        return RestResponse.ok(null, "Delete user successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<ResUserDTO>> getUserById(@PathVariable("id") long id) {
        User user = this.userService.handleGetUserById(id);
        if (user == null) {
            throw new RuntimeException("User với id = " + id + " không tồn tại");
        }

        ResUserDTO userDTO = this.userService.convertToResUserDTO(user);
        return RestResponse.ok(userDTO, "Fetch user by id successfully");
    }

    @GetMapping
    public ResponseEntity<RestResponse<ResultPaginationDTO>> getAllUser(
            Specification<User> spec,
            Pageable pageable) {
        ResultPaginationDTO result = this.userService.handleGetAllUser(spec, pageable);
        return RestResponse.ok(result, "Fetch users successfully");
    }

    @PutMapping
    public ResponseEntity<RestResponse<ResUpdateUserDTO>> updateUser(@RequestBody User user) {
        User updatedUser = this.userService.handleUpdateUser(user);
        if (updatedUser == null) {
            throw new RuntimeException("User với id = " + user.getId() + " không tồn tại");
        }

        ResUpdateUserDTO userDTO = this.userService.convertToResUpdateUserDTO(updatedUser);
        return RestResponse.ok(userDTO, "Update user successfully");
    }
}
