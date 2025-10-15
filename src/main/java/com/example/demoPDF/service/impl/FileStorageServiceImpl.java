package com.example.demoPDF.service.impl;

import com.example.demoPDF.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(@Value("${app.upload.dir:./uploads/pdfs}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void initStorage() {
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage location initialized: {}", this.fileStorageLocation);
        } catch (Exception e) {
            log.error("Could not create the directory where the uploaded files will be stored", e);
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file contains invalid characters
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Generate unique file name to prevent conflicts
            String fileExtension = "";
            if (fileName.lastIndexOf(".") > 0) {
                fileExtension = fileName.substring(fileName.lastIndexOf("."));
            }

            String uniqueFileName = generateUniqueFileName(fileName, fileExtension);

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = this.fileStorageLocation.relativize(targetLocation).toString();
            log.debug("File stored successfully at: {}", relativePath);

            return relativePath;

        } catch (IOException ex) {
            log.error("Could not store file {}. Please try again!", fileName, ex);
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + filePath);
            }
        } catch (MalformedURLException ex) {
            log.error("File not found {}", filePath, ex);
            throw new RuntimeException("File not found " + filePath, ex);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            Files.deleteIfExists(file);
            log.debug("File deleted: {}", filePath);
        } catch (IOException ex) {
            log.error("Could not delete file {}", filePath, ex);
            throw new RuntimeException("Could not delete file " + filePath, ex);
        }
    }

    private String generateUniqueFileName(String originalFileName, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String baseName = originalFileName.replace(extension, "");

        // Clean base name for file system compatibility
        baseName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");

        return String.format("%s_%s_%s%s", baseName, timestamp, uuid, extension);
    }
}
