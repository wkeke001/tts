package com.example.demo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TTSDownloadRequest {

    @NotBlank(message = "文本内容不能为空")
    private String text;

    private String voiceId;

    private String fileName;
}
