package vn.hoidanit.jobhunter.controller;

import java.io.InputStream;
import java.time.Instant;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.jobhunter.domain.response.file.ResUploadFileDTO;
import vn.hoidanit.jobhunter.service.StorageService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.StorageException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final StorageService storageService;

    @PostMapping("/api/v1/files")
    @ApiMessage("Upload single file")
    // Requires authentication - user must be logged in to upload files
    public ResponseEntity<ResUploadFileDTO> uploadFile(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder) throws Exception {

        // Upload file using storage service (MinIO or Local)
        String uploadedFileName = this.storageService.uploadFile(file, folder);

        ResUploadFileDTO res = new ResUploadFileDTO(uploadedFileName, Instant.now());

        return ResponseEntity.ok().body(res);
    }

    @GetMapping("/api/v1/files")
    @ApiMessage("Download a file")
    // Requires authentication - user must be logged in to download files
    public ResponseEntity<Resource> download(
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "folder", required = false) String folder) throws Exception {

        if (fileName == null || folder == null) {
            throw new StorageException("File name or folder is required");
        }

        // check file exist
        long fileLength = this.storageService.getFileSize(fileName, folder);
        if (fileLength == 0) {
            throw new StorageException("File with the name = " + fileName + " not found");
        }

        // download file
        InputStream inputStream = this.storageService.downloadFile(fileName, folder);
        InputStreamResource resource = new InputStreamResource(inputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(fileLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Serve static files from storage (MinIO or Local)
     * Endpoint: /storage/{folder}/{fileName}
     * Example: /storage/company/1716687538974-amzon.jpg
     * This endpoint serves files inline (display in browser) instead of downloading
     */
    @GetMapping("/storage/{folder}/{fileName:.+}")
    @ApiMessage("Serve static file")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String folder,
            @PathVariable String fileName) throws Exception {

        // check file exist
        long fileLength = this.storageService.getFileSize(fileName, folder);
        if (fileLength == 0) {
            throw new StorageException("File with the name = " + fileName + " not found");
        }

        // Get file stream
        InputStream inputStream = this.storageService.downloadFile(fileName, folder);
        InputStreamResource resource = new InputStreamResource(inputStream);

        // Determine content type based on file extension
        String contentType = getContentType(fileName);

        return ResponseEntity.ok()
                .contentLength(fileLength)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    /**
     * Get content type based on file extension
     */
    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "application/octet-stream";
        };
    }
}
