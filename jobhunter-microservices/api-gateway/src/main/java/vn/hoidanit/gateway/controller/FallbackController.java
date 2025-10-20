package vn.hoidanit.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth-service")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        return buildFallbackResponse("Auth Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/company-service")
    public ResponseEntity<Map<String, Object>> companyServiceFallback() {
        return buildFallbackResponse("Company Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/job-service")
    public ResponseEntity<Map<String, Object>> jobServiceFallback() {
        return buildFallbackResponse("Job Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/resume-service")
    public ResponseEntity<Map<String, Object>> resumeServiceFallback() {
        return buildFallbackResponse("Resume Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/file-service")
    public ResponseEntity<Map<String, Object>> fileServiceFallback() {
        return buildFallbackResponse("File Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/notification-service")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        return buildFallbackResponse("Notification Service is currently unavailable. Please try again later.");
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", message);
        response.put("statusCode", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}

