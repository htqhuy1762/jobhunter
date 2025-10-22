package vn.hoidanit.companyservice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify required roles for accessing a controller method
 * Usage:
 * @RequireRole({"ROLE_ADMIN"})
 * @RequireRole({"ROLE_ADMIN", "ROLE_HR"}) // OR logic - user needs ANY of these roles
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /**
     * List of required roles (OR logic - user needs at least one)
     */
    String[] value();

    /**
     * If true, user must have ALL specified roles (AND logic)
     * If false, user needs at least ONE role (OR logic)
     */
    boolean requireAll() default false;
}