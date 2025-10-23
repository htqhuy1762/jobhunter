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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.authservice.domain.User;
import vn.hoidanit.authservice.domain.dto.ReqLoginDTO;
import vn.hoidanit.authservice.domain.dto.ResLoginDTO;
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
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Value("${hoidanit.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @Value("${hoidanit.jwt.cookie-secure}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO) {
        log.info("=== LOGIN ATTEMPT START ===");
        log.info("User attempting login: {}", loginDTO.getUsername());

        try {
            // Check if user exists first
            User userCheck = this.userService.handleGetUserByUsername(loginDTO.getUsername());
            if (userCheck == null) {
                log.error("Login failed: User not found - {}", loginDTO.getUsername());
                throw new RuntimeException("Invalid username or password");
            }
            log.info("User found in database: {} (ID: {})", userCheck.getEmail(), userCheck.getId());
            log.debug("Password from request length: {}", loginDTO.getPassword() != null ? loginDTO.getPassword().length() : 0);
            log.debug("Password hash in DB starts with: {}", userCheck.getPassword().substring(0, Math.min(20, userCheck.getPassword().length())));

            // Authenticate
            log.info("Attempting authentication...");
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(), loginDTO.getPassword());
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authentication successful for user: {}", loginDTO.getUsername());

            // Prepare response
            ResLoginDTO res = new ResLoginDTO();
            User currentUserDB = this.userService.handleGetUserByUsername(loginDTO.getUsername());

        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getName(),
                    currentUserDB.getRole());
            res.setUser(userLogin);
        }

            // Create Access Token
            String accessToken = this.jwtService.createAccessToken(authentication.getName(), res);
            res.setAccessToken(accessToken);

            // Create Refresh Token
            String refreshToken = this.jwtService.createRefreshToken(loginDTO.getUsername(), res);

            // *** LƯU REFRESH TOKEN VÀO REDIS ***
            this.tokenService.saveRefreshToken(loginDTO.getUsername(), refreshToken);
            log.info("Saved refresh token to Redis for user: {}", loginDTO.getUsername());

            // Set cookie
            ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(cookieSecure)  // Read from application.yml
                    .path("/")
                    .maxAge(refreshTokenExpiration)
                    .build();

            log.info("=== LOGIN SUCCESS for user: {} ===", loginDTO.getUsername());
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .body(res);

        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            log.error("=== LOGIN FAILED: Bad credentials for user: {} ===", loginDTO.getUsername());
            log.error("Error message: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("=== LOGIN FAILED: Unexpected error for user: {} ===", loginDTO.getUsername());
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
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

        return ResponseEntity.ok(userGetAccount);
    }

    @GetMapping("/refresh")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refreshToken) {

        log.info("Refresh token request received");

        // Validate refresh token format
        Jwt decodedToken = this.jwtService.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        // *** VALIDATE REFRESH TOKEN IN REDIS ***
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

        // *** XÓA REFRESH TOKEN CŨ TRONG REDIS (QUAN TRỌNG!) ***
        this.tokenService.deleteRefreshToken(email);
        log.info("Deleted old refresh token for user: {}", email);

        // Create new Access Token
        String newAccessToken = this.jwtService.createAccessToken(email, res);
        res.setAccessToken(newAccessToken);

        // Create new Refresh Token
        String newRefreshToken = this.jwtService.createRefreshToken(email, res);

        // *** LƯU REFRESH TOKEN MỚI VÀO REDIS ***
        this.tokenService.saveRefreshToken(email, newRefreshToken);
        log.info("Saved new refresh token to Redis for user: {}", email);

        // Set new cookie
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);

            try {
                Jwt jwt = this.jwtService.decodeToken(accessToken);
                long expiresAt = jwt.getExpiresAt() != null ? jwt.getExpiresAt().getEpochSecond() : 0;
                long now = java.time.Instant.now().getEpochSecond();
                long remainingTime = expiresAt - now;

                if (remainingTime > 0) {
                    this.tokenService.blacklistAccessToken(accessToken, email, remainingTime);
                    log.info("Blacklisted access token for user: {} with remaining time: {}s", email, remainingTime);
                }
            } catch (Exception e) {
                log.warn("Could not blacklist access token for user: {}", email);
            }
        }

        this.tokenService.deleteRefreshToken(email);
        log.info("User logged out, deleted refresh token: {}", email);

        ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<ResLoginDTO> register(@Valid @RequestBody User user) {
        // Check if email exists
        if (this.userService.isEmailExist(user.getEmail())) {
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

        String accessToken = this.jwtService.createAccessToken(newUser.getEmail(), res);
        res.setAccessToken(accessToken);

        String refreshToken = this.jwtService.createRefreshToken(newUser.getEmail(), res);
        this.tokenService.saveRefreshToken(newUser.getEmail(), refreshToken);

        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(res);
    }
}

