package com.example.demo.controller;

import com.example.demo.common.ApiResult;
import com.example.demo.service.CompletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/completions")
public class CompletionController {

    @Autowired
    private CompletionService completionService;

    @PostMapping
    public ApiResult<String> getCompletion(@RequestBody String prompt) {
        String response = completionService.getCompletion(prompt);
        return ApiResult.success(response);
    }
}
