package vn.hoidanit.fileservice.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.fileservice.service.FileService;

/**
 * Controller for serving static files (images, documents) from MinIO
 * This controller does NOT have @RequestMapping prefix to handle root-level /storage/** paths
 */
@RestController
@RequiredArgsConstructor
public class StaticFileController {

    private final FileService fileService;

    /**
     * Serve static files from MinIO storage
     * Endpoint: /storage/{folder}/{fileName}
     * Example: /storage/company/1716687538974-amzon.jpg
     *
     * This endpoint is PUBLIC (no JWT required) to allow serving company logos in job listings
     */
    @GetMapping("/storage/{folder}/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String folder,
            @PathVariable String fileName) throws Exception {

        // Check if file exists and get size
        long fileLength = this.fileService.getFileSize(fileName, folder);
        if (fileLength == 0) {
            throw new RuntimeException("File with the name = " + fileName + " not found");
        }

        // Download file from MinIO
        InputStreamResource resource = this.fileService.downloadFile(fileName, folder);

        // Determine content type based on file extension
        String contentType = getContentType(fileName);

        return ResponseEntity.ok()
                .contentLength(fileLength)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    /**
     * Determine content type based on file extension
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

