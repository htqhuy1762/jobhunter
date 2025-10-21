package vn.hoidanit.fileservice.controller;

import java.time.Instant;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.fileservice.service.FileService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/files")
    public ResponseEntity<ResUploadFileDTO> uploadFile(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder) throws Exception {

        String uploadedFileName = this.fileService.uploadFile(file, folder);
        ResUploadFileDTO res = new ResUploadFileDTO(uploadedFileName, Instant.now());

        return ResponseEntity.ok().body(res);
    }

    @GetMapping("/files")
    public ResponseEntity<Resource> download(
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "folder", required = false) String folder) throws Exception {

        if (fileName == null || folder == null) {
            throw new RuntimeException("File name or folder is required");
        }

        long fileLength = this.fileService.getFileSize(fileName, folder);
        if (fileLength == 0) {
            throw new RuntimeException("File with the name = " + fileName + " not found");
        }

        InputStreamResource resource = this.fileService.downloadFile(fileName, folder);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(fileLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/storage/{folder}/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String folder,
            @PathVariable String fileName) throws Exception {

        long fileLength = this.fileService.getFileSize(fileName, folder);
        if (fileLength == 0) {
            throw new RuntimeException("File with the name = " + fileName + " not found");
        }

        InputStreamResource resource = this.fileService.downloadFile(fileName, folder);
        String contentType = getContentType(fileName);

        return ResponseEntity.ok()
                .contentLength(fileLength)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

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

    public static record ResUploadFileDTO(String fileName, Instant uploadedAt) {}
}


