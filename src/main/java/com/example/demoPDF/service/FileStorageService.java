package com.example.demoPDF.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeFile(MultipartFile file);

    Resource loadFileAsResource(String filePath);

    void deleteFile(String filePath);

    void initStorage();
}