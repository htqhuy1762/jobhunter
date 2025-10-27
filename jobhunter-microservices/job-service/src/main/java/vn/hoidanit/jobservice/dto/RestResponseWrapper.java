package vn.hoidanit.jobservice.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for RestResponse from other services
 * Used for Feign Client responses
 */
@Getter
@Setter
public class RestResponseWrapper<T> {
    private int statusCode;
    private String error;
    private String message;
    private T data;
}

