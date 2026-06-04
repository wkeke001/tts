package com.example.demo.service.impl;

import com.alibaba.dashscope.audio.tts.SpeechSynthesisResult;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.utils.Constants;
import com.example.demo.service.VoiceService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;

@Service
public class VoiceServiceImpl implements VoiceService {

    private static final String AUDIO_MIME_TYPE = "audio/mpeg";

    private static final String CUSTOMIZATION_URL_BEIJING = "https://dashscope.aliyuncs.com/api/v1/services/audio/tts/customization";

    @Value("${dashscope.api-key}")
    private String apiKey;

    private String getApiKey() {
        return apiKey;
    }

    private String toDataUrl(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        String encoded = Base64.getEncoder().encodeToString(bytes);
        return "data:" + AUDIO_MIME_TYPE + ";base64," + encoded;
    }





    @Override
    public void streamTextToSpeechAsync(String text, String voiceId, OutputStream outputStream) throws Exception {
        String model = "cosyvoice-v3.5-plus";
        CountDownLatch latch = new CountDownLatch(1);

        ResultCallback<SpeechSynthesisResult> callback = new ResultCallback<SpeechSynthesisResult>() {
            @Override
            public void onEvent(SpeechSynthesisResult result) {
                if (result.getAudioFrame() != null) {
                    try {
                        outputStream.write(result.getAudioFrame().array());
                        outputStream.flush();
                    } catch (IOException e) {
                        System.out.println("写入音频数据异常：" + e.getMessage());
                    }
                }
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                System.out.println("语音合成异常：" + e.getMessage());
                latch.countDown();
            }
        };

        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                .apiKey(getApiKey())
                .model(model)
                .voice(voiceId)
                .build();

        SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, callback);
        try {
            synthesizer.call(text);
            latch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            synthesizer.getDuplexApi().close(1000, "bye");
        }
    }

    @Override
    public String createVoiceFromUrl(String audioUrl) throws Exception {
        String apiKey = getApiKey();

        String jsonPayload = "{"
                + "\"model\": \"voice-enrollment\","
                + "\"input\": {"
                + "    \"action\": \"create_voice\","
                + "    \"target_model\": \"cosyvoice-v3.5-plus\","
                + "    \"prefix\": \"myvoice\","
                + "    \"url\": \"" + audioUrl + "\","
                + "    \"language_hints\": [\"zh\"]"
                + "}"
                + "}";

        String url = CUSTOMIZATION_URL_BEIJING;
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + apiKey);
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }

        int status = con.getResponseCode();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(status >= 200 && status < 300 ? con.getInputStream() : con.getErrorStream(),
                        StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            if (status == 200) {
                JsonObject jsonObj = new Gson().fromJson(response.toString(), JsonObject.class);
                return jsonObj.toString();
            }
            throw new IOException("创建语音失败: " + status + " - " + response);
        }
    }
}