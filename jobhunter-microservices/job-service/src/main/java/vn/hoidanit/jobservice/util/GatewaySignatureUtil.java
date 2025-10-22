package vn.hoidanit.jobservice.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for verifying Gateway signatures
 * Ensures requests come from API Gateway only
 */
@Slf4j
public class GatewaySignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final long MAX_TIMESTAMP_DIFFERENCE = 300000; // 5 minutes

    public static String generateSignature(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    HMAC_SHA256
            );
            mac.init(secretKeySpec);

            byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error generating signature", e);
            return null;
        }
    }

    public static boolean verifyGatewaySignature(
            String signature,
            String userId,
            String email,
            String timestampStr,
            String secret) {

        if (signature == null || userId == null || email == null || timestampStr == null) {
            log.warn("Missing signature or user context headers");
            return false;
        }

        try {
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = System.currentTimeMillis();

            if (Math.abs(currentTime - timestamp) > MAX_TIMESTAMP_DIFFERENCE) {
                log.warn("SECURITY: Request timestamp expired. Potential replay attack!");
                return false;
            }

            String signatureData = userId + ":" + email + ":" + timestamp;
            String expectedSignature = generateSignature(signatureData, secret);

            if (expectedSignature == null || !expectedSignature.equals(signature)) {
                log.error("SECURITY: Invalid Gateway signature! Potential attack!");
                return false;
            }

            return true;

        } catch (NumberFormatException e) {
            log.error("Invalid timestamp format", e);
            return false;
        }
    }
}

