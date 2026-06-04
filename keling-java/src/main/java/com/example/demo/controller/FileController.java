package com.example.demo.controller;

import com.example.demo.common.ApiResult;
import com.example.demo.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/gif")
    public ApiResult<Map<String, Object>> uploadGif(@RequestParam("file") MultipartFile file) {
        Map<String, Object> data = fileService.uploadAndProcessGif(file);
        return ApiResult.success(data);
    }

    @PostMapping("/analyze")
    public ApiResult<String> analyzeImage(@RequestParam("fileUrl") String fileUrl) {
        String response = fileService.analyzeImage(fileUrl);
        return ApiResult.success(response);
    }

    @PostMapping("/gif-frame")
    public ApiResult<Map<String, Object>> uploadGifFirstFrame(@RequestParam("file") MultipartFile file) {
        Map<String, Object> data = fileService.uploadGifFirstFrame(file);
        return ApiResult.success(data);
    }
}
