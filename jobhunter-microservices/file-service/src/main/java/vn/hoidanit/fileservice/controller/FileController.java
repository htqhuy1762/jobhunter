package vn.hoidanit.fileservice.controller;

import java.time.Instant;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import vn.hoidanit.fileservice.annotation.RateLimit;
import vn.hoidanit.fileservice.domain.response.RestResponse;
import vn.hoidanit.fileservice.service.FileService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/files")
    @RateLimit(name = "uploadFile")
    public ResponseEntity<RestResponse<ResUploadFileDTO>> uploadFile(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder) throws Exception {

        String uploadedFileName = this.fileService.uploadFile(file, folder);
        ResUploadFileDTO res = new ResUploadFileDTO(uploadedFileName, Instant.now());
        return RestResponse.ok(res, "Upload file successfully");
    }

    @GetMapping("/files")
    @RateLimit(name = "downloadFile")
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


    public static record ResUploadFileDTO(String fileName, Instant uploadedAt) {}
}


