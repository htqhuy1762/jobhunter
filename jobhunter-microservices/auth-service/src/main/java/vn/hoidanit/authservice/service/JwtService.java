package vn.hoidanit.authservice.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.authservice.domain.dto.ResLoginDTO;
import vn.hoidanit.authservice.util.SecurityUtil;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_USER = "user";
    private static final String CLAIM_PERMISSION = "permission";

    private final JwtEncoder jwtEncoder;

    @Value("${hoidanit.jwt.base64-secret}")
    private String jwtKey;

    @Value("${hoidanit.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    @Value("${hoidanit.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public String createAccessToken(String email, ResLoginDTO dto) {
        Instant now = Instant.now();
        Instant validity = now.plus(accessTokenExpiration, ChronoUnit.SECONDS);

        ResLoginDTO.UserInsideToken userToken = createUserToken(dto);
        List<String> authorities = extractAuthorities(dto);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim(CLAIM_USER, userToken)
                .claim(CLAIM_PERMISSION, authorities)
                .build();

        return encodeToken(claims);
    }

    public String createRefreshToken(String email, ResLoginDTO dto) {
        Instant now = Instant.now();
        Instant validity = now.plus(refreshTokenExpiration, ChronoUnit.SECONDS);

        ResLoginDTO.UserInsideToken userToken = createUserToken(dto);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim(CLAIM_USER, userToken)
                .build();

        return encodeToken(claims);
    }

    public Jwt checkValidRefreshToken(String token) {
        return decodeToken(token);
    }

    public Jwt decodeToken(String token) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            throw new RuntimeException("Token invalid");
        }
    }

    private ResLoginDTO.UserInsideToken createUserToken(ResLoginDTO dto) {
        ResLoginDTO.UserInsideToken userToken = new ResLoginDTO.UserInsideToken();
        userToken.setId(dto.getUser().getId());
        userToken.setEmail(dto.getUser().getEmail());
        userToken.setName(dto.getUser().getName());
        return userToken;
    }

    private List<String> extractAuthorities(ResLoginDTO dto) {
        List<String> authorities = new ArrayList<>();

        try {
            if (dto.getUser().getRole() != null) {
                if (dto.getUser().getRole().getName() != null) {
                    authorities.add(dto.getUser().getRole().getName());
                }

                if (dto.getUser().getRole().getPermissions() != null) {
                    dto.getUser().getRole().getPermissions().forEach(permission -> {
                        if (permission != null && permission.getName() != null) {
                            authorities.add(permission.getName());
                        }
                    });
                }
            }
        } catch (Exception e) {
            // Handle LazyInitializationException - continue with current authorities
        }

        return authorities;
    }

    private String encodeToken(JwtClaimsSet claims) {
        JwsHeader jwsHeader = JwsHeader.with(SecurityUtil.JWT_ALGORITHM).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(jwtKey);
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, SecurityUtil.JWT_ALGORITHM.getName());
    }
}

