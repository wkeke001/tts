package com.example.demo.service;

import java.io.File;
import java.io.OutputStream;

public interface VoiceService {
     void streamTextToSpeechAsync(String text, String voiceId, OutputStream outputStream) throws Exception;
    String createVoiceFromUrl(String audioUrl) throws Exception;
}