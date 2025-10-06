package vn.hoidanit.jobhunter.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để đánh dấu endpoint cần rate limiting
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * Số lượng request tối đa trong khoảng thời gian
     */
    int limit() default 10;

    /**
     * Thời gian window tính bằng giây
     */
    int duration() default 60;

    /**
     * Key prefix cho Redis (mặc định sử dụng IP address)
     */
    String keyPrefix() default "rate_limit";

    /**
     * Message trả về khi bị rate limit
     */
    String message() default "Too many requests. Please try again later.";
}

