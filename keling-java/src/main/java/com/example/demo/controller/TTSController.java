package com.example.demo.controller;

import com.example.demo.common.ApiResult;
import com.example.demo.dto.TTSDownloadRequest;
import com.example.demo.dto.VoiceRequest;
import com.example.demo.service.CompletionService;
import com.example.demo.service.VoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/tts")
public class TTSController {

    @Autowired
    private VoiceService voiceService;

    @Autowired
    private CompletionService completionService;

    @PostMapping("/synthesis")
    public ResponseEntity<StreamingResponseBody> synthesis(@Valid @RequestBody TTSDownloadRequest request) {
        String voiceId = resolveVoiceId(request.getVoiceId());
        String fileName = request.getFileName();
        if (fileName == null || fileName.isEmpty()) {
            fileName = "stream_output.wav";
        }

        String text = request.getText();
        StreamingResponseBody stream = outputStream -> {
            try {
                voiceService.streamTextToSpeechAsync(text, voiceId, outputStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.valueOf("audio/wav"))
                .body(stream);
    }

    @PostMapping("/voices")
    public ApiResult<String> createVoice(@RequestParam("url") String audioUrl) throws Exception {
        String voiceId = voiceService.createVoiceFromUrl(audioUrl);
        return ApiResult.success(voiceId);
    }

    @PostMapping("/synthesis-from-completion")
    public ResponseEntity<StreamingResponseBody> synthesisFromCompletion(@Valid @RequestBody VoiceRequest request) {
        String text = completionService.getCompletion(request.getPrompt());

        TTSDownloadRequest ttsRequest = new TTSDownloadRequest();
        ttsRequest.setText(text);
        ttsRequest.setVoiceId(request.getType());

        return synthesis(ttsRequest);
    }

    private String resolveVoiceId(String voiceId) {
        if ("bubu".equals(voiceId)) {
            return "cosyvoice-v3.5-plus-myvoice2-b6178f211e6c43dabbd64d31bb2ed47c";
        } else if ("yier".equals(voiceId)) {
            return "cosyvoice-v3.5-plus-myvoice1-d0a1d6fdf9564d98a8e6bfea39f7ac3d";
        }
        return voiceId;
    }
}
