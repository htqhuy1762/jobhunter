package vn.hoidanit.jobhunter.service;

import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import vn.hoidanit.jobhunter.util.error.StorageException;

/**
 * Interface cho storage service
 * Cho phép chuyển đổi giữa Local Storage và MinIO
 */
public interface StorageService {

    /**
     * Upload file
     * @param file File cần upload
     * @param folder Thư mục lưu trữ
     * @return Tên file đã upload
     */
    String uploadFile(MultipartFile file, String folder) throws Exception;

    /**
     * Download file
     * @param fileName Tên file
     * @param folder Thư mục chứa file
     * @return InputStream của file
     */
    InputStream downloadFile(String fileName, String folder) throws Exception;

    /**
     * Lấy kích thước file
     * @param fileName Tên file
     * @param folder Thư mục chứa file
     * @return Kích thước file (bytes)
     */
    long getFileSize(String fileName, String folder) throws Exception;
}

