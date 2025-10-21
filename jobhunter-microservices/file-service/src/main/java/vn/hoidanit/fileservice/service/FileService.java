package vn.hoidanit.fileservice.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileService {
    @Value("${file.upload-dir:file:///D:/Jobhunter/storage/}")
    private String baseURI;

    public void createDirectory(String folder) throws URISyntaxException {
        URI uri = new URI(folder);
        Path path = Paths.get(uri);
        File tmpDir = new File(path.toString());
        if (!tmpDir.isDirectory()) {
            try {
                Files.createDirectory(tmpDir.toPath());
                log.info(">>> CREATE NEW DIRECTORY SUCCESSFUL, PATH = {}", folder);
            } catch (IOException e) {
                log.error(">>> Error creating directory: {}", e.getMessage());
            }
        } else {
            log.info(">>> SKIP MAKING DIRECTORY, ALREADY EXISTS: {}", folder);
        }
    }

    public String uploadFile(MultipartFile file, String folder) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");
        boolean isValid = allowedExtensions.stream()
                .anyMatch(item -> fileName.toLowerCase().endsWith(item));

        if (!isValid) {
            throw new RuntimeException("File extension is not allowed. Only accept: " + allowedExtensions.toString());
        }

        createDirectory(baseURI + folder);
        return store(file, folder);
    }

    public String store(MultipartFile file, String folder) throws URISyntaxException, IOException {
        String finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
        URI uri = new URI(baseURI + folder + "/" + finalName);
        Path path = Paths.get(uri);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }
        return finalName;
    }

    public long getFileSize(String fileName, String folder) throws URISyntaxException {
        URI uri = new URI(baseURI + folder + "/" + fileName);
        Path path = Paths.get(uri);
        File tmpDir = new File(path.toString());

        if(!tmpDir.exists() || tmpDir.isDirectory()) {
            return 0;
        }
        return tmpDir.length();
    }

    public InputStreamResource downloadFile(String fileName, String folder) throws URISyntaxException, FileNotFoundException {
        URI uri = new URI(baseURI + folder + "/" + fileName);
        Path path = Paths.get(uri);
        File file = new File(path.toString());

        return new InputStreamResource(new FileInputStream(file));
    }
}