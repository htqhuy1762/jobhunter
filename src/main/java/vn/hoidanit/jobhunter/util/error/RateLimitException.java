package vn.hoidanit.jobhunter.util.error;

/**
 * Exception khi vượt quá rate limit
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}

