package vn.hoidanit.gateway.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for generating and verifying Gateway signatures
 * Used to ensure requests to internal services come from API Gateway only
 */
@Slf4j
public class SignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Generate HMAC-SHA256 signature
     * @param data Data to sign
     * @param secret Secret key
     * @return Base64 encoded signature
     */
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
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * Verify signature
     * @param data Original data
     * @param signature Signature to verify
     * @param secret Secret key
     * @return true if valid, false otherwise
     */
    public static boolean verifySignature(String data, String signature, String secret) {
        if (signature == null || data == null) {
            return false;
        }

        String expectedSignature = generateSignature(data, secret);
        return signature.equals(expectedSignature);
    }

    /**
     * Generate signature data from user context
     * Format: userId:email:timestamp
     */
    public static String createSignatureData(String userId, String email, long timestamp) {
        return userId + ":" + email + ":" + timestamp;
    }
}

