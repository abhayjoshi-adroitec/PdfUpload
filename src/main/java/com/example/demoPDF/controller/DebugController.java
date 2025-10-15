package com.example.demoPDF.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello! Controller is working. Context path is working.";
    }

    @GetMapping("/test-pdf-mapping")
    public String testPdfMapping() {
        return "PDF mapping test successful!";
    }

    // Test what URLs are actually being mapped
    @PostMapping("/test-upload")
    public String testUpload() {
        return "Upload endpoint reachable!";
    }
}