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

        long fileLength = this.fileService.getFileSize(fileName, folder);
        if (fileLength == 0) {
            throw new RuntimeException("File with the name = " + fileName + " not found");
        }

        InputStreamResource resource = this.fileService.downloadFile(fileName, folder);
        String contentType = determineContentType(fileName);

        return ResponseEntity.ok()
                .contentLength(fileLength)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    private String determineContentType(String fileName) {
        String lowerFileName = fileName.toLowerCase();

        if (lowerFileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFileName.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerFileName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return "application/octet-stream";
    }
}

