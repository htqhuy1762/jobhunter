package vn.hoidanit.jobservice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for Pageable parameters with frontend-friendly 1-based page indexing.
 * This annotation automatically converts 1-based page numbers from frontend to 0-based for Spring Data.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PageableDefault {
    /**
     * Default page number (1-based)
     */
    int page() default 1;

    /**
     * Default page size
     */
    int size() default 10;

    /**
     * Default sort property
     */
    String sort() default "id";

    /**
     * Default sort direction
     */
    String direction() default "desc";
}

