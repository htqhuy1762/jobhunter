package vn.hoidanit.companyservice.exception;

/**
 * Exception thrown when user doesn't have sufficient permissions
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

