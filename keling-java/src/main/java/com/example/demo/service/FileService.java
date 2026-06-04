package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileService {

    Map<String, Object> uploadAndProcessGif(MultipartFile file);

    String analyzeImage(String fileUrl);

    Map<String, Object> uploadGifFirstFrame(MultipartFile file);
}
