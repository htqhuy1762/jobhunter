package vn.hoidanit.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/auth-service")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        log.warn("Auth Service is unavailable - fallback triggered");
        return createFallbackResponse("Auth Service");
    }

    @GetMapping("/company-service")
    public ResponseEntity<Map<String, Object>> companyServiceFallback() {
        log.warn("Company Service is unavailable - fallback triggered");
        return createFallbackResponse("Company Service");
    }

    @GetMapping("/job-service")
    public ResponseEntity<Map<String, Object>> jobServiceFallback() {
        log.warn("Job Service is unavailable - fallback triggered");
        return createFallbackResponse("Job Service");
    }

    @GetMapping("/resume-service")
    public ResponseEntity<Map<String, Object>> resumeServiceFallback() {
        log.warn("Resume Service is unavailable - fallback triggered");
        return createFallbackResponse("Resume Service");
    }

    @GetMapping("/file-service")
    public ResponseEntity<Map<String, Object>> fileServiceFallback() {
        log.warn("File Service is unavailable - fallback triggered");
        return createFallbackResponse("File Service");
    }

    @GetMapping("/notification-service")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        log.warn("Notification Service is unavailable - fallback triggered");
        return createFallbackResponse("Notification Service");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", serviceName + " is currently unavailable. Please try again later.");
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("timestamp", Instant.now());
        response.put("service", serviceName);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}

