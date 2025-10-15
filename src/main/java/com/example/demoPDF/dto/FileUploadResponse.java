package com.example.demoPDF.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private Long documentId;
    private String filename;
    private Long fileSize;
    private Integer pageCount;
    private String message;
}