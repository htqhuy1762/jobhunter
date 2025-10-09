package vn.hoidanit.jobhunter.controller;

import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

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
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users/{id}")
    @ApiMessage("Get user by id")
    @Secured({"SUPER_ADMIN", "ROLE_ADMIN"})
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") long id) throws IdInvalidException {
        User user = this.userService.handleGetUserById(id);
        if (user == null) {
            throw new IdInvalidException("User with id " + id + " not found");
            
        }
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertToResUserDTO(user));
    }

    @GetMapping("/users")
    @ApiMessage("Get all users")
    @Secured({"SUPER_ADMIN", "ROLE_ADMIN"})
    public ResponseEntity<ResultPaginationDTO> getAllUser(@Filter Specification<User> spec, Pageable pageable) {
        
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleGetAllUser(spec, pageable));
    }

    @PostMapping("/users")
    @ApiMessage("Create new user")
    @Secured({"SUPER_ADMIN", "ROLE_ADMIN"})
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody User postManUser) throws IdInvalidException {

        boolean isEmailExist = this.userService.isEmailExist(postManUser.getEmail());
        if(isEmailExist) {
            throw new IdInvalidException("Email " + postManUser.getEmail() + " is already exist");
        }

        String hashPassword = this.passwordEncoder.encode(postManUser.getPassword());
        postManUser.setPassword(hashPassword);
        User newUser = this.userService.handleCreateUser(postManUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResCreateUserDTO(newUser));
    }

    @PutMapping("/users")
    @ApiMessage("Update a user")
    @Secured({"SUPER_ADMIN", "ROLE_ADMIN"})
    public ResponseEntity<ResUpdateUserDTO> updateUser(@RequestBody User postManUser) throws IdInvalidException {
        User updatedUser = this.userService.handleUpdateUser(postManUser);
        if (updatedUser == null) {
            throw new IdInvalidException("User with id " + postManUser.getId() + " not found");
            
        }
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertToResUpdateUserDTO(updatedUser));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete user by id")
    @Secured({"SUPER_ADMIN", "ROLE_ADMIN"})
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) throws IdInvalidException {
        User user = this.userService.handleGetUserById(id);
        if (user == null) {
            throw new IdInvalidException("User with id " + id + " not found");
        }
        
        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }
}
