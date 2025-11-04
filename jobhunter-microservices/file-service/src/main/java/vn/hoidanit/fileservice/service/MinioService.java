package vn.hoidanit.fileservice.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @PostConstruct
    public void init() {
        try {
            // Create bucket if it doesn't exist
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Bucket '{}' created successfully", bucketName);
            } else {
                log.info("Bucket '{}' already exists", bucketName);
            }
        } catch (Exception e) {
            log.error("Error initializing MinIO bucket: {}", e.getMessage());
            throw new RuntimeException("Could not initialize MinIO bucket", e);
        }
    }

    /**
     * Upload file to MinIO with Circuit Breaker and Retry
     */
    @CircuitBreaker(name = "minioService", fallbackMethod = "uploadFileFallback")
    @Retry(name = "minioService")
    public String uploadFile(MultipartFile file, String folder) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String filename = System.currentTimeMillis() + "-" + UUID.randomUUID() + extension;
        String objectName = folder + "/" + filename;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("File uploaded successfully: {}", objectName);
            return objectName;
        }
    }

    /**
     * Fallback method for file upload
     * IMPORTANT: Parameter must be Throwable, not Exception
     */
    private String uploadFileFallback(MultipartFile file, String folder, Throwable ex) {
        log.error("Circuit breaker fallback triggered for file upload: {}", ex.getMessage());
        log.debug("Exception type: {}", ex.getClass().getName());
        throw new RuntimeException("MinIO service is currently unavailable. Please try again later.", ex);
    }

    /**
     * Download file from MinIO with Circuit Breaker and Retry
     */
    @CircuitBreaker(name = "minioService", fallbackMethod = "downloadFileFallback")
    @Retry(name = "minioService")
    public InputStream downloadFile(String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    /**
     * Fallback method for file download
     * IMPORTANT: Parameter must be Throwable, not Exception
     */
    private InputStream downloadFileFallback(String objectName, Throwable ex) {
        log.error("Circuit breaker fallback triggered for file download: {}", ex.getMessage());
        log.debug("Exception type: {}", ex.getClass().getName());
        throw new RuntimeException("MinIO service is currently unavailable. Cannot download file.", ex);
    }

    /**
     * Delete file from MinIO
     */
    public void deleteFile(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
        log.info("File deleted successfully: {}", objectName);
    }

    /**
     * Get presigned URL for file access (valid for 7 days)
     */
    public String getPresignedUrl(String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(7, TimeUnit.DAYS)
                        .build()
        );
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get file size in bytes
     */
    public long getFileSize(String objectName) throws Exception {
        var stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
        return stat.size();
    }
}


