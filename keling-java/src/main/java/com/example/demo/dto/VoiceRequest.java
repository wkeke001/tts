package com.example.demo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class VoiceRequest {

    @NotBlank(message = "prompt 不能为空")
    private String prompt;

    private String type;
}
