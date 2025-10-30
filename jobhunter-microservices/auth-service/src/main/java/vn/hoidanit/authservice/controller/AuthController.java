package vn.hoidanit.authservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.authservice.annotation.RateLimit;
import vn.hoidanit.authservice.domain.User;
import vn.hoidanit.authservice.domain.dto.ReqChangePasswordDTO;
import vn.hoidanit.authservice.domain.dto.ReqLoginDTO;
import vn.hoidanit.authservice.domain.dto.ResLoginDTO;
import vn.hoidanit.authservice.domain.response.RestResponse;
import vn.hoidanit.authservice.service.JwtService;
import vn.hoidanit.authservice.service.TokenService;
import vn.hoidanit.authservice.service.UserService;
import vn.hoidanit.authservice.util.SecurityUtil;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtService jwtService;
    private final UserService userService;
    private final TokenService tokenService;

    @Value("${hoidanit.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @Value("${hoidanit.jwt.cookie-secure}")
    private boolean cookieSecure;

    @RateLimit(name = "login")
    @PostMapping("/login")
    public ResponseEntity<RestResponse<ResLoginDTO>> login(@Valid @RequestBody ReqLoginDTO loginDTO) {
        log.info("Login attempt for user: {}", loginDTO.getUsername());

        // Check if user exists - LOAD WITH PERMISSIONS
        User currentUserDB = this.userService.handleGetUserByUsernameWithPermissions(loginDTO.getUsername());
        if (currentUserDB == null) {
            log.error("Login failed: User not found - {}", loginDTO.getUsername());
            throw new RuntimeException("Invalid username or password");
        }

        // Authenticate
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Prepare response
        ResLoginDTO res = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getName(),
                currentUserDB.getRole());
        res.setUser(userLogin);

        // Create tokens
        String accessToken = this.jwtService.createAccessToken(authentication.getName(), res);
        res.setAccessToken(accessToken);

        String refreshToken = this.jwtService.createRefreshToken(loginDTO.getUsername(), res);
        this.tokenService.saveRefreshToken(loginDTO.getUsername(), refreshToken);

        // Set cookie
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        log.info("Login successful for user: {}", loginDTO.getUsername());

        RestResponse<ResLoginDTO> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Login successfully");
        response.setData(res);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(response);
    }

    @GetMapping("/account")
    public ResponseEntity<RestResponse<ResLoginDTO.UserGetAccount>> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        log.info("Fetching account info for user: {}", email);

        User currentUserDB = this.userService.handleGetUserByUsername(email);

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();

        if (currentUserDB != null) {
            userLogin.setId(currentUserDB.getId());
            userLogin.setEmail(currentUserDB.getEmail());
            userLogin.setName(currentUserDB.getName());
            userLogin.setRole(currentUserDB.getRole());
            userGetAccount.setUser(userLogin);
        }

        return RestResponse.ok(userGetAccount, "Fetch account successfully");
    }

    @GetMapping("/refresh")
    public ResponseEntity<RestResponse<ResLoginDTO>> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refreshToken) {

        log.info("Refresh token request received");

        // Validate refresh token
        Jwt decodedToken = this.jwtService.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        boolean isValid = this.tokenService.validateRefreshToken(email, refreshToken);
        if (!isValid) {
            log.error("Refresh token invalid or expired for user: {}", email);
            throw new RuntimeException("Refresh token is invalid");
        }

        // Get user info
        User currentUserDB = this.userService.handleGetUserByUsername(email);
        ResLoginDTO res = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getName(),
                currentUserDB.getRole());
        res.setUser(userLogin);

        // Delete old refresh token
        this.tokenService.deleteRefreshToken(email);

        // Create new tokens
        String newAccessToken = this.jwtService.createAccessToken(email, res);
        res.setAccessToken(newAccessToken);

        String newRefreshToken = this.jwtService.createRefreshToken(email, res);
        this.tokenService.saveRefreshToken(email, newRefreshToken);

        // Set new cookie
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        log.info("Refresh token successful for user: {}", email);

        RestResponse<ResLoginDTO> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Refresh token successfully");
        response.setData(res);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<RestResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");

        // Blacklist access token if present
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);

            try {
                Jwt jwt = this.jwtService.decodeToken(accessToken);
                long expiresAt = jwt.getExpiresAt() != null ? jwt.getExpiresAt().getEpochSecond() : 0;
                long now = java.time.Instant.now().getEpochSecond();
                long remainingTime = expiresAt - now;

                if (remainingTime > 0) {
                    this.tokenService.blacklistAccessToken(accessToken, email, remainingTime);
                    log.info("Blacklisted access token for user: {}", email);
                }
            } catch (Exception e) {
                log.warn("Could not blacklist access token for user: {}", email);
            }
        }

        // Delete refresh token
        this.tokenService.deleteRefreshToken(email);
        log.info("User logged out: {}", email);

        // Clear cookie
        ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .build();

        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Logout successfully");
        response.setData(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RestResponse<ResLoginDTO>> register(@Valid @RequestBody User user) {
        log.info("Register attempt for email: {}", user.getEmail());

        // Check if email exists
        if (this.userService.isEmailExist(user.getEmail())) {
            log.error("Register failed: Email already exists - {}", user.getEmail());
            throw new RuntimeException("Email đã tồn tại");
        }

        // Create user
        User newUser = this.userService.handleCreateUser(user);

        // Auto login after register
        ResLoginDTO res = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                newUser.getId(),
                newUser.getEmail(),
                newUser.getName(),
                newUser.getRole());
        res.setUser(userLogin);

        // Create tokens
        String accessToken = this.jwtService.createAccessToken(newUser.getEmail(), res);
        res.setAccessToken(accessToken);

        String refreshToken = this.jwtService.createRefreshToken(newUser.getEmail(), res);
        this.tokenService.saveRefreshToken(newUser.getEmail(), refreshToken);

        // Set cookie
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        log.info("Register successful for user: {}", newUser.getEmail());

        RestResponse<ResLoginDTO> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.CREATED.value());
        response.setMessage("Register successfully");
        response.setData(res);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(response);
    }

    @PostMapping("/change-password")
    @RateLimit(name = "changePassword")
    public ResponseEntity<RestResponse<Void>> changePassword(
            @Valid @RequestBody ReqChangePasswordDTO changePasswordDTO) {

        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        log.info("Change password request for user: {}", email);

        if (email.isEmpty()) {
            log.error("Change password failed: User not authenticated");
            throw new RuntimeException("User not authenticated");
        }

        // Validate new password and confirm password match
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            log.error("Change password failed: New password and confirm password do not match for user {}", email);
            throw new RuntimeException("New password and confirm password do not match");
        }

        // Get current user
        User currentUser = this.userService.handleGetUserByUsername(email);
        if (currentUser == null) {
            log.error("Change password failed: User not found - {}", email);
            throw new RuntimeException("User not found");
        }

        // Verify old password
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, changePasswordDTO.getOldPassword());
            authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (Exception e) {
            log.error("Change password failed: Old password incorrect for user {}", email);
            throw new RuntimeException("Old password is incorrect");
        }

        // Update password
        boolean updated = this.userService.updatePassword(currentUser, changePasswordDTO.getNewPassword());
        if (!updated) {
            log.error("Change password failed: Could not update password for user {}", email);
            throw new RuntimeException("Could not update password");
        }

        log.info("Password changed successfully for user: {}", email);
        return RestResponse.ok(null, "Change password successfully");
    }
}

