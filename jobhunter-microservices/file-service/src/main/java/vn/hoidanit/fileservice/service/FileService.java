package vn.hoidanit.fileservice.service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.StatObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final MinioService minioService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String folder) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");
        boolean isValid = allowedExtensions.stream()
                .anyMatch(item -> fileName != null && fileName.toLowerCase().endsWith(item));

        if (!isValid) {
            throw new RuntimeException("File extension is not allowed. Only accept: " + allowedExtensions.toString());
        }

        // Upload to MinIO and return the object name (path)
        String objectName = minioService.uploadFile(file, folder);
        log.info("File uploaded to MinIO: {}", objectName);
        return objectName;
    }

    public long getFileSize(String fileName, String folder) {
        String objectName = folder + "/" + fileName;
        try {
            return minioService.getFileSize(objectName);
        } catch (Exception e) {
            log.error("Error checking file size: {}", e.getMessage());
            return 0;
        }
    }

    public InputStreamResource downloadFile(String fileName, String folder) throws Exception {
        String objectName = folder + "/" + fileName;
        InputStream inputStream = minioService.downloadFile(objectName);
        return new InputStreamResource(inputStream);
    }
}