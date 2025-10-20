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
import vn.hoidanit.authservice.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody User user) {
        boolean isEmailExist = this.userService.isEmailExist(user.getEmail());
        if (isEmailExist) {
            throw new RuntimeException("Email " + user.getEmail() + " đã tồn tại");
        }

        User newUser = this.userService.handleCreateUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResCreateUserDTO(newUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) {
        User currentUser = this.userService.handleGetUserById(id);
        if (currentUser == null) {
            throw new RuntimeException("User với id = " + id + " không tồn tại");
        }

        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") long id) {
        User user = this.userService.handleGetUserById(id);
        if (user == null) {
            throw new RuntimeException("User với id = " + id + " không tồn tại");
        }

        return ResponseEntity.ok(this.userService.convertToResUserDTO(user));
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllUser(
            Specification<User> spec,
            Pageable pageable) {
        return ResponseEntity.ok(this.userService.handleGetAllUser(spec, pageable));
    }

    @PutMapping
    public ResponseEntity<ResUpdateUserDTO> updateUser(@RequestBody User user) {
        User updatedUser = this.userService.handleUpdateUser(user);
        if (updatedUser == null) {
            throw new RuntimeException("User với id = " + user.getId() + " không tồn tại");
        }
        return ResponseEntity.ok(this.userService.convertToResUpdateUserDTO(updatedUser));
    }
}
