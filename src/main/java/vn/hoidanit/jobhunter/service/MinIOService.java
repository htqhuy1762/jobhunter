package vn.hoidanit.jobhunter.service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.util.error.StorageException;

@Service
@Slf4j
@ConditionalOnProperty(name = "storage.mode", havingValue = "minio")
@RequiredArgsConstructor
public class MinIOService implements StorageService {
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;


    /**
     * Kiểm tra và tạo bucket nếu chưa tồn tại
     */
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info(">>> CREATE NEW MINIO BUCKET SUCCESSFUL: {}", bucketName);
            } else {
                log.info(">>> MINIO BUCKET ALREADY EXISTS: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Error checking/creating bucket: {}", e.getMessage());
            throw new RuntimeException("Failed to ensure bucket exists", e);
        }
    }

    /**
     * Upload file lên MinIO
     * @param file MultipartFile từ request
     * @param folder Thư mục trong bucket (vd: company, resume)
     * @return Tên file đã upload
     */
    @Override
    public String uploadFile(MultipartFile file, String folder) throws Exception {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                throw new StorageException("File is empty");
            }

            String fileName = file.getOriginalFilename();
            List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");
            boolean isValid = allowedExtensions.stream()
                    .anyMatch(item -> fileName.toLowerCase().endsWith(item));

            if (!isValid) {
                throw new StorageException("File extension is not allowed. Only accept: " + allowedExtensions.toString());
            }

            // Tạo tên file unique
            String finalName = System.currentTimeMillis() + "-" + fileName;
            String objectName = folder + "/" + finalName;

            // Ensure bucket exists
            ensureBucketExists();

            // Upload file
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            log.info(">>> UPLOAD FILE TO MINIO SUCCESS: {}", objectName);
            return finalName;

        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error uploading file to MinIO: {}", e.getMessage());
            throw new StorageException("Failed to upload file to MinIO: " + e.getMessage());
        }
    }

    /**
     * Download file từ MinIO
     * @param fileName Tên file
     * @param folder Thư mục chứa file
     * @return InputStream của file
     */
    @Override
    public InputStream downloadFile(String fileName, String folder) throws Exception {
        try {
            String objectName = folder + "/" + fileName;

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            log.error("Error downloading file from MinIO: {}", e.getMessage());
            throw new StorageException("File not found or error downloading: " + e.getMessage());
        }
    }

    /**
     * Lấy kích thước file
     * @param fileName Tên file
     * @param folder Thư mục chứa file
     * @return Kích thước file (bytes)
     */
    @Override
    public long getFileSize(String fileName, String folder) throws Exception {
        try {
            String objectName = folder + "/" + fileName;

            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());

            return stat.size();
        } catch (Exception e) {
            log.error("Error getting file size from MinIO: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Lấy URL public của file (nếu bucket được set public)
     * Hoặc có thể generate presigned URL
     */
    public String getFileUrl(String fileName, String folder) {
        // MinIO URL format: http://localhost:9000/bucket-name/folder/filename
        return String.format("%s/%s/%s/%s",
                endpoint, bucketName, folder, fileName);
    }
}